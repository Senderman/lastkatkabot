package com.senderman.lastkatkabot.handlers

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.TgUser
import com.senderman.lastkatkabot.DBService.UserType
import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.logging.BotLogger
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AdminHandler(private val handler: LastkatkaBotHandler) {
    fun addUser(message: Message, type: UserType) {
        if (!message.isReply) return
        val id = message.replyToMessage.from.id
        val list: MutableSet<Int>
        val format: String
        when (type) {
            UserType.ADMINS -> {
                list = handler.admins
                format = "✅ %1\$s теперь мой хозяин!"
            }
            UserType.BLACKLIST -> {
                if (handler.isFromAdmin(message.replyToMessage) || handler.isPremiumUser(message.replyToMessage)) {
                    handler.sendMessage(message.chatId, "Мы таких в плохие киси не берем!")
                    return
                }
                list = handler.blacklist
                format = "\uD83D\uDE3E %1\$s - плохая киса!"
            }
            UserType.PREMIUM -> {
                list = handler.premiumUsers
                format = "\uD83D\uDC51 %1\$s теперь премиум пользователь!"
            }
        }

        val name = message.replyToMessage.from.firstName
        val user = TgUser(id, name)
        list.add(id)
        Services.db.addTgUser(id, type)
        handler.sendMessage(message.chatId, String.format(format, user.name))
    }

    fun listUsers(message: Message, type: UserType) {
        val users = Services.db.getTgUsersByType(type)
        val messageToSend = Methods.sendMessage().setChatId(message.chatId)
        var allAdminsAccess = false
        val title: String
        val callback: String

        when (type) {
            UserType.ADMINS -> {
                title = "\uD83D\uDE0E <b>Админы бота:</b>\n"
                callback = LastkatkaBot.CALLBACK_DELETE_ADMIN
            }
            UserType.BLACKLIST -> {
                allAdminsAccess = true
                title = "\uD83D\uDE3E <b>Список плохих кис:</b>\n"
                callback = LastkatkaBot.CALLBACK_DELETE_NEKO
            }
            UserType.PREMIUM -> {
                title = "\uD83D\uDC51 <b>Список премиум-пользователей:</b>\n"
                callback = LastkatkaBot.CALLBACK_DELETE_PREM
            }

        }
        val showButtons = allAdminsAccess || message.chatId == Services.botConfig.mainAdmin.toLong()
        if (!showButtons || !message.isUserMessage) {
            val userlist = StringBuilder(title)
            val dropList = StringBuilder("\n")
            for (id in users) {
                try {
                    val name = Methods.getChatMember(id.toLong(), id).call(handler).user.firstName
                    val user = TgUser(id, name)
                    userlist.append(user.link).append("\n")
                } catch (e: Exception) {
                    Services.db.removeTGUser(id, type)
                    dropList.append("Юзер с id $id удален из бд!\n")
                }
            }
            messageToSend.setText(userlist.append(dropList).toString())

        } else {
            val markup = InlineKeyboardMarkup()
            val rows = ArrayList<List<InlineKeyboardButton>>()
            var row: MutableList<InlineKeyboardButton> = ArrayList()
            for (id in users) {
                val name = Methods.getChatMember(id.toLong(), id).call(handler).user.firstName
                val user = TgUser(id, name)
                row.add(InlineKeyboardButton()
                        .setText(user.name)
                        .setCallbackData(callback + " " + user.id))
                if (row.size == 2) {
                    rows.add(row)
                    row = ArrayList()
                }
            }
            if (row.size == 1) {
                rows.add(row)
            }
            rows.add(listOf(InlineKeyboardButton()
                    .setText("Закрыть меню")
                    .setCallbackData(LastkatkaBot.CALLBACK_CLOSE_MENU)))
            markup.keyboard = rows
            messageToSend.setText(title + "Для удаления пользователя нажмите на него").replyMarkup = markup
        }
        handler.sendMessage(messageToSend)
    }

    fun goodneko(message: Message) {
        if (!message.isReply) return
        val neko = TgUser(message.replyToMessage.from.id, message.replyToMessage.from.firstName)
        Services.db.removeTGUser(neko.id, UserType.BLACKLIST)
        handler.blacklist.remove(message.replyToMessage.from.id)
        handler.sendMessage(message.chatId, "\uD83D\uDE38 ${neko.link} - хорошая киса!")
    }

    fun update(message: Message) {
        val params = message.text.split("\n")
        if (params.size < 2) {
            handler.sendMessage(message.chatId, "Неверное количество аргументов!")
            return
        }
        val update = StringBuilder().append("\uD83D\uDCE3 <b>ВАЖНОЕ ОБНОВЛЕНИЕ:</b> \n\n")
        for (i in 1 until params.size) {
            update.append("* ${params[i]}\n")
        }
        val chats = Services.db.getChatIdsSet()
        chats.remove(Services.botConfig.tourgroup)
        for (chat in chats) {
            handler.sendMessage(chat, update.toString())
        }
    }

    fun cleanChats() {
        handler.sendMessage(Services.botConfig.mainAdmin, "Очистка чатов...")
        val chats = Services.db.getChatTitleMap()
        val cpus = Runtime.getRuntime().availableProcessors()
        val executor = Executors.newFixedThreadPool(cpus)
        executor.invokeAll(splitCleanupTasks(cpus, chats))
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.MINUTES)
        Services.db.cleanup()
        handler.sendMessage(Services.botConfig.mainAdmin, "Чаты обновлены!")
    }

    private fun splitCleanupTasks(workers: Int, chats: Map<Long, String>): List<Callable<Unit>> {
        val partSize = chats.size / workers
        val list = ArrayList<Callable<Unit>>()
        for (i in 0 until workers - 1) {
            list.add(cleanupTask(i * partSize, (i + 1) * partSize, chats))
        }
        list.add(cleanupTask((workers - 1) * partSize, chats.size, chats))
        return list
    }

    private fun cleanupTask(start: Int, bound: Int, chats: Map<Long, String>): Callable<Unit> {
        return Callable {
            val keys = chats.keys.toList()
            for (i in start until bound) {
                val chatId = keys[i]
                try {
                    val chatMsg = handler.execute(SendMessage(chatId, "Сервисное сообщение, оно будет удалено через пару секунд"))
                    val title = chatMsg.chat.title
                    Methods.deleteMessage(chatId, chatMsg.messageId).call(handler)
                    Services.db.updateTitle(chatId, title)
                } catch (e: TelegramApiException) {
                    Services.db.removeChat(chatId)
                    Methods.leaveChat(chatId).call(handler)
                    handler.sendMessage(Services.botConfig.mainAdmin, "Чат \"${chats[chatId]}\" удален из списка!")
                }
            }
        }
    }

    fun announce(message: Message) {
        handler.sendMessage(message.chatId, "Рассылка запущена. На время рассылки бот будет недоступен")
        var text = message.text
        text = "\uD83D\uDCE3 <b>Объявление</b>\n\n" + text.split("\\s+".toRegex(), 2)[1]
        val usersIds = Services.db.getAllUsersIds()
        var counter = 0
        for (userId in usersIds) {
            try {
                handler.execute(SendMessage(userId.toLong(), text).enableHtml(true))
                counter++
            } catch (e: TelegramApiException) {
                BotLogger.error("ANNOUNCE", e.toString())
            }
        }
        handler.sendMessage(message.chatId, "Объявление получили $counter/${usersIds.size} человек")
    }

    fun setupHelp(message: Message) {
        handler.sendMessage(message.chatId, Services.botConfig.setupHelp)
    }

}