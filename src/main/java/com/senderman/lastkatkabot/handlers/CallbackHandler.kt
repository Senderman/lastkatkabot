package com.senderman.lastkatkabot.handlers

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.TgUser
import com.senderman.lastkatkabot.DBService.UserType
import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

class CallbackHandler(private val handler: LastkatkaBotHandler) {

    private fun answerQuery(id: String, text: String, showAlert: Boolean = true) {
        Methods.answerCallbackQuery()
                .setCallbackQueryId(id)
                .setText(text)
                .setShowAlert(showAlert)
                .call(handler)
    }

    private fun editText(query: CallbackQuery, text: String, markup: InlineKeyboardMarkup? = null) {
        editText(query.message.chatId, query.message.messageId, text, markup)
    }

    private fun editText(chatId: Long, messageId: Int, text: String, markup: InlineKeyboardMarkup? = null) {
        Methods.editMessageText()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setText(text)
                .setReplyMarkup(markup)
                .call(handler)
    }

    fun payRespects(query: CallbackQuery) {
        if (query.message.text.contains(query.from.firstName)) {
            answerQuery(query.id, "You've already payed respects! (or you've tried to pay respects to yourself)")
            return
        }
        answerQuery(query.id, "You've payed respects")
        editText(query, "${query.message.text}\n${query.from.firstName} has payed respects", UsercommandsHandler.getMarkupForPayingRespects())
    }

    fun cake(query: CallbackQuery, actions: CakeAcion) {
        if (query.from.id != query.message.replyToMessage.from.id) {
            answerQuery(query.id, "Этот тортик не вам!")
            return
        }
        if (query.message.date + 2400 < System.currentTimeMillis() / 1000) {
            answerQuery(query.id, "Тортик испортился!")
            editText(query, "\uD83E\uDD22 Тортик попытались взять, но он испортился!")
            return
        }
        val acq = Methods.answerCallbackQuery()
                .setCallbackQueryId(query.id)
        val emt = Methods.editMessageText()
                .setChatId(query.message.chatId)
                .setMessageId(query.message.messageId)
                .setReplyMarkup(null)
        if (actions == CakeAcion.CAKE_OK) {
            acq.text = "n p u я m н o r o  a n n e m u m a"
            emt.setText("\uD83C\uDF82 " + query.from.firstName + " принял тортик"
                    + query.data.replace(LastkatkaBot.CALLBACK_CAKE_OK, ""))
        } else {
            acq.text = "Ну и ладно"
            emt.setText("\uD83D\uDEAB \uD83C\uDF82 " + query.from.firstName + " отказался от тортика"
                    + query.data.replace(LastkatkaBot.CALLBACK_CAKE_NOT, ""))
        }
        acq.call(handler)
        emt.call(handler)
    }

    fun registerInTournament(query: CallbackQuery) {
        val memberId = query.from.id
        if (!handler.tournamentHandler.isEnabled) {
            answerQuery(query.id, "⚠️ На данный момент нет открытых раундов!")
            return
        }
        if (memberId in handler.tournamentHandler.membersIds) {
            answerQuery(query.id, "⚠️ Вы уже получили разрешение на отправку сообщений!")
            return
        }
        if (query.from.userName !in handler.tournamentHandler.members) {
            answerQuery(query.id, "\uD83D\uDEAB Вы не являетесь участником текущего раунда!")
            return
        }
        handler.tournamentHandler.membersIds.add(memberId)
        Methods.Administration.restrictChatMember()
                .setChatId(Services.botConfig.tourgroup)
                .setUserId(memberId)
                .setCanSendMessages(true)
                .setCanSendMediaMessages(true)
                .setCanSendOtherMessages(true)
                .call(handler)
        answerQuery(query.id, "✅ Вам даны права на отправку сообщений в группе турнира!")
        handler.sendMessage(Services.botConfig.tourgroup, "✅ ${query.from.firstName} получил доступ к игре!")
    }

    fun addChat(query: CallbackQuery) {
        val chatId = query.data.replace(LastkatkaBot.CALLBACK_ALLOW_CHAT, "").toLong()
        handler.allowedChats.add(chatId)
        editText(query, "✅ Чат добавлен в разрешенные!")
        val message = handler.sendMessage(chatId, "Разработчик принял данный чат. Бот готов к работе здесь!\n" +
                "Для некоторых фичей бота требуются права админа на удаление и закреп сообщений.")
        Services.db.addAllowedChat(chatId, message.chat.title)
    }

    fun denyChat(query: CallbackQuery) {
        val chatId = query.data.replace(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT, "").toLong()
        editText(query, "\uD83D\uDEAB Чат отклонен!")
        handler.sendMessage(chatId, "Разработчик отклонил данный чат. Всем пока!")
        Methods.leaveChat(chatId).call(handler)
    }

    fun deleteChat(query: CallbackQuery) {
        val chatId = query.data.split(" ")[1].toLong()
        Services.db.removeAllowedChat(chatId)
        Services.db.deleteBncGame(chatId)
        handler.allowedChats.remove(chatId)
        answerQuery(query.id, "Чат удален!")
        handler.sendMessage(chatId, "Разработчик решил удалить бота из данного чата. Всем пока!")
        Methods.leaveChat(chatId).call(handler)
        Methods.deleteMessage(query.message.chatId, query.message.messageId).call(handler)
    }

    fun deleteUser(query: CallbackQuery, type: UserType?) {
        val userIds: MutableSet<Int>
        val listName: String
        when (type) {
            UserType.ADMINS -> {
                userIds = handler.admins
                listName = "админов"
            }
            UserType.BLACKLIST -> {
                userIds = handler.blacklist
                listName = "плохих кошечек"
            }
            UserType.PREMIUM -> {
                userIds = handler.premiumUsers
                listName = "премиум-пользователей"
            }
            else -> return
        }
        val userId = query.data.split(" ")[1].toInt()
        Services.db.removeTGUser(userId, type)
        userIds.remove(userId)
        answerQuery(query.id, "Пользователь удален из списка")
        handler.sendMessage(userId, "Разработчик удалил вас из $listName бота!")
        Methods.deleteMessage(query.message.chatId, query.message.messageId).call(handler)
    }

    private fun notFor(message: Message, user: User): Boolean = false/*{
        if (!message.hasEntities()) return false
        for (entity in message.entities) {
            if (entity?.user?.id == user.id) return false
        }
        return true
    }*/

    fun acceptMarriage(query: CallbackQuery) {
        val userId = query.from.id
        val message = query.message
        if (notFor(message, query.from) || !message.isReply || message.replyToMessage.from.id != userId) {
            answerQuery(query.id, "Куда лезете? Это не вам!")
            return
        }
        if (Services.db.getLover(userId) != 0) {
            answerQuery(query.id, "У вас уже есть вторая половинка!")
            return
        }
        answerQuery(query.id, "Поздравляем! Теперь у вас есть вторая половинка")
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        // user - acceptor, couple - inviter
        val user = TgUser(userId, query.from.firstName)
        val coupleId = query.data.replace(LastkatkaBot.CALLBACK_ACCEPT_MARRIAGE.toRegex(), "").toInt()
        val couple = TgUser(Methods.getChatMember(message.chatId, coupleId).call(handler).user)
        Services.db.setLover(user.id, couple.id)
        handler.sendMessage(couple.id, "Поздравляем! Теперь ваша вторая половинка - " + user.getLink())
        val format = "Внимание все! Сегодня великий день свадьбы %s и %s! Так давайте же поздравим их и съедим шавуху в часть такого праздника!"
        val text = String.format(format, user.getLink(), couple.getLink())
        handler.sendMessage(message.chatId, text)
    }

    fun denyMarriage(query: CallbackQuery) {
        val userId = query.from.id
        val message = query.message
        if (notFor(message, query.from) || !message.isReply || message.replyToMessage.from.id != userId) {
            answerQuery(query.id, "Куда лезете? Это не вам!")
            return
        }
        answerQuery(query.id, "Такое упускаете...")
        editText(query, "Пользователь ${query.from.firstName} отказался от брака :(")
    }

    fun closeMenu(query: CallbackQuery) = editText(query, "Меню закрыто")


    enum class CakeAcion {
        CAKE_OK, CAKE_NOT
    }

}