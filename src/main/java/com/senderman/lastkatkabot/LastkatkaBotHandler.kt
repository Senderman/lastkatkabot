package com.senderman.lastkatkabot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod
import com.senderman.Command
import com.senderman.TgUser
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import java.awt.*
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
    val allowedChats: MutableSet<Long>
    val bullsAndCowsGames: MutableMap<Long, BullsAndCowsGame>
    val duels: MutableMap<String, Duel>
    val commands: MutableMap<String, Method>
    val tournamentHandler: TournamentHandler
    val userRows: MutableMap<Long, UserRow>

    init {
        val mainAdmin = Services.config().mainAdmin
        sendMessage(mainAdmin.toLong(), "Initialization...")

        // settings
        Services.setHandler(this)
        Services.setDBService(MongoDBService())
        Services.db().cleanup()

        admins = Services.db().getTgUsersByType(UserType.ADMINS)
        premiumUsers = Services.db().getTgUsersByType(UserType.PREMIUM)
        blacklist = Services.db().getTgUsersByType(UserType.BLACKLIST)
        allowedChats = Services.db().getAllowedChatsSet()
        allowedChats.add(Services.config().lastvegan)
        allowedChats.add(Services.config().tourgroup)
        commands = HashMap()
        adminHandler = AdminHandler(this)
        callbackHandler = CallbackHandler(this)
        tournamentHandler = TournamentHandler(this)
        bullsAndCowsGames = Services.db().getBnCGames()
        userRows = Services.db().getUserRows()
        duels = HashMap()

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
        if (message.migrateFromChatId != null && allowedChats.contains(message.migrateFromChatId)) {
            migrateChat(message.migrateFromChatId, chatId)
            sendMessage(message.migrateFromChatId, "Id чата обновлен!")
        }

        // do not respond in not allowed chats
        if (!allowedChats.contains(chatId) && !message.isUserMessage)
            return null

        if (message.leftChatMember != null && message.leftChatMember.userName != botUsername && message.chatId != Services.config().tourgroup) {
            Methods.sendDocument()
                    .setChatId(chatId)
                    .setFile(Services.config().leavesticker!!)
                    .setReplyToMessageId(message.messageId)
                    .call(this)
            Services.db().removeUserFromChatDB(message.leftChatMember.id, chatId)
        }

        // update current userrow in chat if exists
        if (!message.isUserMessage) {
            userRows[chatId]?.addUser(message)
        }

        if (!message.hasText()) return null

        if (message.isGroupMessage || message.isSuperGroupMessage) // add user to DB
            Services.db().addUserToChatDB(message)

        val text = message.text

        // for bulls and cows
        if (text.matches(Regex("\\d{4,10}")) && !isInBlacklist(message)) {
            bullsAndCowsGames[chatId]?.check(message)
            return null
        }

        if (!message.isCommand) return null

        /* bot should only trigger on general commands (like /command) or on commands for this bot (/command@mybot),
         * and NOT on commands for another bots (like /command@notmybot)
         */
        val command = text.split(Regex("\\s+"), 2)[0]
                .toLowerCase(Locale.ENGLISH)
                .replace("@$botUsername", "")
        if (command.contains("@")) return null

        // find method by name and invoke it
        try {
            if (!commands.contains(command)) return null
            val method = commands.getValue(command)
            val annotation = method.getAnnotation(Command::class.java)
            if (message.from.id != Services.config().mainAdmin && annotation.forMainAdmin) {
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
        return Services.config().username!!.split(" ")[Services.config().position]
    }

    override fun getBotToken(): String {
        return Services.config().token!!.split(" ")[Services.config().position]
    }

    private fun processCallbackQuery(query: CallbackQuery) {
        val data = query.data
        when {
            data.startsWith(LastkatkaBot.CALLBACK_CAKE_OK) ->
                callbackHandler.cake(query, CallbackHandler.CAKE_ACTIONS.CAKE_OK)

            data.startsWith(LastkatkaBot.CALLBACK_CAKE_NOT) ->
                callbackHandler.cake(query, CallbackHandler.CAKE_ACTIONS.CAKE_NOT)

            data.startsWith(LastkatkaBot.CALLBACK_ALLOW_CHAT) ->
                callbackHandler.addChat(query)

            data.startsWith(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT) ->
                callbackHandler.denyChat(query)

            data.startsWith(LastkatkaBot.CALLBACK_ACCEPT_MARRIAGE) ->
                callbackHandler.accept_marriage(query)

            data.startsWith(LastkatkaBot.CALLBACK_DELETE_CHAT) -> {

                callbackHandler.deleteChat(query)
                adminHandler.chats(query.message)
            }

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
                LastkatkaBot.CALLBACK_DENY_MARRIAGE ->
                    callbackHandler.deny_marriage(query)

                LastkatkaBot.CALLBACK_VOTE_BNC -> bullsAndCowsGames[query.message.chatId]?.addVote(query)
            }
        }
    }

    private fun processNewMembers(message: Message) {
        val chatId = message.chatId
        val newMembers = message.newChatMembers

        if (chatId == Services.config().tourgroup) {
            for (user in newMembers) {
                // restrict any user who isn't in tournament
                if (user.id !in tournamentHandler.membersIds) {
                    Methods.Administration.restrictChatMember()
                            .setChatId(Services.config().tourgroup)
                            .setUserId(user.id)
                            .setCanSendMessages(false).call(this)
                }
            }
        } else if (!newMembers[0].bot) {
            // say hi
            val membername = newMembers[0].firstName
            if (membername.length <= 8) {
                try {
                    val sticker = getHelloSticker(membername)
                    Methods.sendDocument(chatId)
                            .setFile(sticker)
                            .setReplyToMessageId(message.messageId)
                            .call(this) // send senko
                    sticker.delete()
                    return
                } catch (ignored: IOException) {
                }
            }

            Methods.sendDocument(chatId)
                    .setFile(Services.config().higif!!)
                    .setReplyToMessageId(message.messageId)
                    .call(this)

        } else if (newMembers[0].userName == botUsername) {
            // Say hello to new group if chat is allowed
            if (chatId in allowedChats) {
                sendMessage(chatId, "Этот чат находится в списке разрешенных. Бот готов к работе здесь")
                return
            }

            sendMessage(chatId, "Чата нет в списке разрешенных. Дождитесь решения разработчика")

            val row1 = listOf(InlineKeyboardButton()
                    .setText("Добавить")
                    .setCallbackData(LastkatkaBot.CALLBACK_ALLOW_CHAT + chatId))
            val row2 = listOf(InlineKeyboardButton()
                    .setText("Отклонить")
                    .setCallbackData(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT + chatId))
            val markup = InlineKeyboardMarkup()
            markup.keyboard = listOf(row1, row2)
            val inviter = TgUser(message.from.id, message.from.firstName)
            sendMessage(Methods.sendMessage(Services.config().mainAdmin.toLong(), String.format("Добавить чат %1\$s (%2\$d) в список разрешенных? - %3\$s",
                    message.chat.title, chatId, inviter.link))
                    .setReplyMarkup(markup))
        }
    }

    @Throws(IOException::class)
    private fun getHelloSticker(name: String): File {
        val image = ImageIO.read(File("res/senko.png"))
        val g = image.graphics as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        val font = Font(Font.SANS_SERIF, Font.BOLD, 40)
        val frc = g.fontRenderContext
        val nameOutline = font.createGlyphVector(frc, "$name!").getOutline(315f, 170f)
        g.color = Color.BLACK
        g.stroke = BasicStroke(2.5f)
        g.draw(nameOutline)
        g.color = Color.WHITE
        g.fill(nameOutline)
        g.dispose()
        val out = File("senko.webp")
        ImageIO.write(image, "webp", out)
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

    private fun migrateChat(oldChatId: Long, newChatId: Long) {
        allowedChats.remove(oldChatId)
        allowedChats.add(newChatId)
        Services.db().updateChatId(oldChatId, newChatId)
    }

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