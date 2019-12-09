package com.senderman.lastkatkabot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod
import com.senderman.Command
import com.senderman.lastkatkabot.DBService.UserType
import com.senderman.lastkatkabot.handlers.AdminHandler
import com.senderman.lastkatkabot.handlers.CallbackHandler
import com.senderman.lastkatkabot.handlers.TournamentHandler
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame
import com.senderman.lastkatkabot.tempobjects.Duel
import com.senderman.lastkatkabot.tempobjects.UserRow
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.HashMap

class LastkatkaBotHandler internal constructor() : BotHandler() {
    private val commandListener: CommandListener
    private val adminHandler: AdminHandler
    private val callbackHandler: CallbackHandler
    val admins: MutableSet<Int>
    val blacklist: MutableSet<Int>
    val premiumUsers: MutableSet<Int>
    val bullsAndCowsGames: MutableMap<Long, BullsAndCowsGame>
    val duels: MutableMap<String, Duel>
    val commands: MutableMap<String, Method>
    val tournamentHandler: TournamentHandler
    val userRows: MutableMap<Long, UserRow>
    var feedbackUserId = 0

    init {
        val mainAdmin = Services.botConfig.mainAdmin
        sendMessage(mainAdmin, "Инициализация...")

        // settings
        Services.handler = this
        Services.db = MongoDBService()

        admins = Services.db.getTgUsersByType(UserType.ADMINS)
        premiumUsers = Services.db.getTgUsersByType(UserType.PREMIUM)
        blacklist = Services.db.getTgUsersByType(UserType.BLACKLIST)
        commands = HashMap()
        adminHandler = AdminHandler(this)
        callbackHandler = CallbackHandler(this)
        tournamentHandler = TournamentHandler(this)
        bullsAndCowsGames = Services.db.getBnCGames()
        userRows = Services.db.getUserRows()
        duels = HashMap()
        sendMessage(mainAdmin, "Очистка бд от мусора...")
        adminHandler.cleanChats()

        // init command-method map
        commandListener = CommandListener(this, adminHandler, tournamentHandler)
        val annotationClass = Command::class.java
        for (m in commandListener.javaClass.declaredMethods) {
            if (m.isAnnotationPresent(annotationClass))
                commands[m.getAnnotation(annotationClass).name] = m
        }
        sendMessage(mainAdmin, "Бот готов к работе!")
    }

    public override fun onUpdate(update: Update): BotApiMethod<*>? { // first we will handle callbacks
        if (update.hasCallbackQuery()) {
            processCallbackQuery(update.callbackQuery)
            return null
        }

        if (!update.hasMessage()) return null

        val message = update.message
        // don't handle old messages
        if (message.date + 120 < System.currentTimeMillis() / 1000) return null

        val newMembers = message.newChatMembers
        if (newMembers != null && newMembers.size != 0) {
            processNewMembers(message)
            return null
        }

        val chatId = message.chatId

        // migrate cats if needed
        if (message.migrateFromChatId != null) {
            migrateChat(message.migrateFromChatId, chatId)
            sendMessage(message.migrateFromChatId, "Id чата обновлен!")
        }

        if (message.leftChatMember != null && message.leftChatMember.userName != botUsername && message.chatId != Services.botConfig.tourgroup) {
            Methods.sendDocument()
                    .setChatId(chatId)
                    .setFile(Services.botConfig.leavesticker)
                    .setReplyToMessageId(message.messageId)
                    .call(this)
            Services.db.removeUserFromChatDB(message.leftChatMember.id, chatId)
        }

        // update current userrow in chat if exists
        if (!message.isUserMessage)
            userRows[chatId]?.addUser(message.from)

        if (!message.hasText()) return null

        if (message.isGroupMessage || message.isSuperGroupMessage) // add user to DB
            Services.db.addUserToChatDB(message)

        val text = message.text

        // for bulls and cows
        if (text.matches(Regex("\\d{4,10}")) && !isInBlacklist(message)) {
            bullsAndCowsGames[chatId]?.check(message)
            return null
        }

        // for answering feedbacks
        if (message.chatId == Services.botConfig.mainAdmin.toLong() && feedbackUserId != 0) {
            val answer = """
                🔔 <b>Ответ разработчика</b>
                
                $text
            """.trimIndent()
            sendMessage(feedbackUserId, answer)
            feedbackUserId = 0
            return null
        }

        if (!message.isCommand) return null

        /* bot should only trigger on general commands (like /command) or on commands for this bot (/command@mybot),
         * and NOT on commands for another bots (like /command@notmybot)
         */
        val command = text.split("\\s+".toRegex(), 2)[0]
                .toLowerCase(Locale.ENGLISH)
                .replace("@$botUsername", "")
        if ("@" in command) return null

        // find method by name and invoke it
        try {
            if (command !in commands) return null
            val method = commands.getValue(command)
            val annotation = method.getAnnotation(Command::class.java)
            if (message.from.id != Services.botConfig.mainAdmin && annotation.forMainAdmin) {
                return null
            } else if (annotation.forAllAdmins && !isFromAdmin(message)) {
                return null
            } else if (annotation.forPremium && !isPremiumUser(message)) {
                return null
            } else if (isInBlacklist(message)) return null
            method.invoke(commandListener, message)
        } catch (e: Exception) {
            return null
        }

        return null
    }

    override fun getBotUsername(): String {
        return Services.botConfig.username.split(" ")[Services.botConfig.position]
    }

    override fun getBotToken(): String {
        return Services.botConfig.token.split(" ")[Services.botConfig.position]
    }

    private fun processCallbackQuery(query: CallbackQuery) {
        val data = query.data
        when {
            data.startsWith(LastkatkaBot.CALLBACK_CAKE_OK) ->
                callbackHandler.cake(query, CallbackHandler.CakeAcion.CAKE_OK)

            data.startsWith(LastkatkaBot.CALLBACK_CAKE_NOT) ->
                callbackHandler.cake(query, CallbackHandler.CakeAcion.CAKE_NOT)

            data.startsWith(LastkatkaBot.CALLBACK_ACCEPT_MARRIAGE) ->
                callbackHandler.acceptMarriage(query)

            data.startsWith(LastkatkaBot.CALLBACK_DENY_MARRIAGE) ->
                callbackHandler.denyMarriage(query)

            data.startsWith("deleteuser_") -> {
                val type: UserType = when (query.data.split(" ")[0]) {
                    LastkatkaBot.CALLBACK_DELETE_ADMIN -> UserType.ADMINS
                    LastkatkaBot.CALLBACK_DELETE_NEKO -> UserType.BLACKLIST
                    LastkatkaBot.CALLBACK_DELETE_PREM -> UserType.PREMIUM
                    else -> return
                }
                callbackHandler.deleteUser(query, type)
                adminHandler.listUsers(query.message, type)
            }

            else -> when (data) {
                LastkatkaBot.CALLBACK_REGISTER_IN_TOURNAMENT ->
                    callbackHandler.registerInTournament(query)

                LastkatkaBot.CALLBACK_PAY_RESPECTS ->
                    callbackHandler.payRespects(query)

                LastkatkaBot.CALLBACK_CLOSE_MENU ->
                    callbackHandler.closeMenu(query)

                LastkatkaBot.CALLBACK_JOIN_DUEL -> {
                    val message = query.message
                    val duel = duels[message.chatId.toString() + " " + message.messageId]
                    if (duel == null) {
                        Duel.answerCallbackQuery(query, "⏰ Дуэль устарела!", true)
                        return
                    }
                    duel.join(query)
                    return
                }

                LastkatkaBot.CALLBACK_VOTE_BNC -> bullsAndCowsGames[query.message.chatId]?.addVote(query)
            }
        }
    }

    private fun processNewMembers(message: Message) {
        val chatId = message.chatId
        val newMembers = message.newChatMembers

        if (newMembers[0].userName == botUsername) {
            sendMessage(chatId,
                    "Всем привет! Для полноценного использования всех моих фичей дайте мне права на пин и удаление сообщений, пожалуйста!")
        } else if (chatId == Services.botConfig.tourgroup) {
            for (user in newMembers) {
                // restrict any user who isn't in tournament
                if (user.id !in tournamentHandler.membersIds) {
                    Methods.Administration.restrictChatMember()
                            .setChatId(Services.botConfig.tourgroup)
                            .setUserId(user.id)
                            .setCanSendMessages(false).call(this)
                }
            }
        } else if (!newMembers[0].bot) {
            // say hi
            val membername = newMembers[0].firstName
            try {
                val sticker = getHelloSticker(membername)
                Methods.sendDocument(chatId)
                        .setFile(sticker)
                        .setReplyToMessageId(message.messageId)
                        .call(this) // send sticker
                sticker.delete()
                return
            } catch (e: IOException) {
                Methods.sendDocument(chatId)
                        .setFile(Services.botConfig.higif)
                        .setReplyToMessageId(message.messageId)
                        .call(this)
            }
        }
    }

    @Throws(IOException::class)
    private fun getHelloSticker(name: String): File {
        val orig = javaClass.getResourceAsStream("/menhera.png")
        val img = ImageIO.read(orig)
        orig.close()
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        val font = Font(Font.SANS_SERIF, Font.BOLD, 45)
        val text = font.createGlyphVector(g.fontRenderContext, "${name}!")
        val imageWidth = img.width
        val textWidth = text.outline.bounds.width
        if (imageWidth < textWidth) throw Exception()
        val x = (imageWidth - textWidth) / 2
        val textOutline = text.getOutline(x.toFloat(), 480f)
        g.color = Color.black
        g.stroke = BasicStroke(3.5f)
        g.draw(textOutline)
        g.color = Color.white
        g.fill(textOutline)
        g.dispose()
        val out = File("sticker.webp")
        ImageIO.write(img, "webp", out)
        return out
    }

    fun isFromAdmin(message: Message): Boolean = message.from.id in admins

    fun isPremiumUser(message: Message): Boolean = message.from.id in premiumUsers

    private fun isInBlacklist(message: Message): Boolean {
        val result = message.from.id in blacklist
        if (result) {
            Methods.deleteMessage(message.chatId, message.messageId).call(this)
        }
        return result
    }

    private fun migrateChat(oldChatId: Long, newChatId: Long) = Services.db.updateChatId(oldChatId, newChatId)

    fun sendMessage(chatId: Int, text: String): Message = sendMessage(chatId.toLong(), text)

    fun sendMessage(chatId: Long, text: String): Message {
        return sendMessage(Methods.sendMessage(chatId, text))
    }

    fun sendMessage(sm: SendMessageMethod): Message {
        val sendMessage = SendMessage(sm.chatId, sm.text)
                .enableHtml(true)
                .disableWebPagePreview()
                .setReplyMarkup(sm.replyMarkup)
                .setReplyToMessageId(sm.replyToMessageId)
        return try {
            execute(sendMessage)
        } catch (e: TelegramApiRequestException) {
            migrateChat(sm.chatId.toLong(), e.parameters.migrateToChatId)
            sm.setChatId(e.parameters.migrateToChatId)
            return sm.call(this)
        }
    }
}
