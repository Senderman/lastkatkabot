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

class CallbackHandler(private val handler: LastkatkaBotHandler) {
    fun payRespects(query: CallbackQuery) {
        if (query.message.text.contains(query.from.firstName)) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.id)
                    .setText("You've already payed respects! (or you've tried to pay respects to yourself)")
                    .setShowAlert(true)
                    .call(handler)
            return
        }
        Methods.answerCallbackQuery()
                .setCallbackQueryId(query.id)
                .setText("You've payed respects")
                .setShowAlert(true)
                .call(handler)
        Methods.editMessageText()
                .setChatId(query.message.chatId)
                .setMessageId(query.message.messageId)
                .setReplyMarkup(UsercommandsHandler.getMarkupForPayingRespects())
                .setText(query.message.text + "\n" + query.from.firstName + " has payed respects")
                .call(handler)
    }

    fun cake(query: CallbackQuery, actions: CAKE_ACTIONS) {
        if (query.from.id != query.message.replyToMessage.from.id) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.id)
                    .setText("Этот тортик не вам!")
                    .setShowAlert(true)
                    .call(handler)
            return
        }
        if (query.message.date + 2400 < System.currentTimeMillis() / 1000) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.id)
                    .setText("Тортик испортился!")
                    .setShowAlert(true)
                    .call(handler)
            Methods.editMessageText()
                    .setChatId(query.message.chatId)
                    .setText("\uD83E\uDD22 Тортик попытались взять, но он испортился!")
                    .setMessageId(query.message.messageId)
                    .setReplyMarkup(null)
                    .call(handler)
            return
        }
        val acq = Methods.answerCallbackQuery()
                .setCallbackQueryId(query.id)
        val emt = Methods.editMessageText()
                .setChatId(query.message.chatId)
                .setMessageId(query.message.messageId)
                .setReplyMarkup(null)
        if (actions == CAKE_ACTIONS.CAKE_OK) {
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
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.id)
                    .setText("⚠️ На данный момент нет открытых раундов!")
                    .setShowAlert(true)
                    .call(handler)
            return
        }
        if (handler.tournamentHandler.membersIds.contains(memberId)) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.id)
                    .setText("⚠️ Вы уже получили разрешение на отправку сообщений!")
                    .setShowAlert(true)
                    .call(handler)
            return
        }
        if (!handler.tournamentHandler.members.contains(query.from.userName)) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.id)
                    .setText("\uD83D\uDEAB Вы не являетесь участником текущего раунда!")
                    .setShowAlert(true)
                    .call(handler)
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
        Methods.answerCallbackQuery()
                .setCallbackQueryId(query.id)
                .setText("✅ Вам даны права на отправку сообщений в группе турнира!")
                .setShowAlert(true)
                .call(handler)
        handler.sendMessage(Services.botConfig.tourgroup, String.format("✅ %1\$s получил доступ к игре!", query.from.firstName))
    }

    fun addChat(query: CallbackQuery) {
        val chatId = query.data.replace(LastkatkaBot.CALLBACK_ALLOW_CHAT, "").toLong()
        handler.allowedChats.add(chatId)
        Methods.editMessageText()
                .setChatId(query.message.chatId)
                .setText("✅ Чат добавлен в разрешенные!")
                .setMessageId(query.message.messageId)
                .setReplyMarkup(null)
                .call(handler)
        val message = handler.sendMessage(chatId, "Разработчик принял данный чат. Бот готов к работе здесь!\n" +
                "Для некоторых фичей бота требуются права админа на удаление и закреп сообщений.")
        Services.db.addAllowedChat(chatId, message.chat.title)
    }

    fun denyChat(query: CallbackQuery) {
        val chatId = query.data.replace(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT, "").toLong()
        Methods.editMessageText()
                .setChatId(query.message.chatId)
                .setMessageId(query.message.messageId)
                .setText("\uD83D\uDEAB Чат отклонен!")
                .setReplyMarkup(null)
                .call(handler)
        handler.sendMessage(chatId, "Разработчик отклонил данный чат. Всем пока!")
        Methods.leaveChat(chatId).call(handler)
    }

    fun deleteChat(query: CallbackQuery) {
        val chatId = query.data.split(" ").toTypedArray()[1].toLong()
        Services.db.removeAllowedChat(chatId)
        Services.db.deleteBncGame(chatId)
        handler.allowedChats.remove(chatId)
        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText("Чат удален!")
                .setCallbackQueryId(query.id)
                .call(handler)
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
        val userId = query.data.split(" ").toTypedArray()[1].toInt()
        Services.db.removeTGUser(userId, type)
        userIds.remove(userId)
        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText("Пользователь удален из списка")
                .setCallbackQueryId(query.id)
                .call(handler)
        handler.sendMessage(userId, "Разработчик удалил вас из $listName бота!")
        Methods.deleteMessage(query.message.chatId, query.message.messageId).call(handler)
    }

    private fun notFor(message: Message, user: User): Boolean {
        if (!message.hasEntities()) return true
        for (entity in message.entities) {
            if (entity.type != "text_mention") continue
            if (entity.user.id == user.id) return false
        }
        return true
    }

    fun accept_marriage(query: CallbackQuery) {
        val userId = query.from.id
        val message = query.message
        if (notFor(message, query.from) || !message.isReply || message.replyToMessage.from.id != userId) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("Куда лезете? Это не вам!")
                    .setCallbackQueryId(query.id)
                    .call(handler)
            return
        }
        if (Services.db.getLover(userId) != 0) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("У вас уже есть вторая половинка!")
                    .setCallbackQueryId(query.id)
                    .call(handler)
            return
        }
        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText("Поздравляем! Теперь у вас есть вторая половинка")
                .setCallbackQueryId(query.id)
                .call(handler)
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

    fun deny_marriage(query: CallbackQuery) {
        val userId = query.from.id
        val message = query.message
        if (notFor(message, query.from) || !message.isReply || message.replyToMessage.from.id != userId) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("Куда лезете? Это не вам!")
                    .setCallbackQueryId(query.id)
                    .call(handler)
            return
        }
        Methods.answerCallbackQuery()
                .setShowAlert(false)
                .setText("Такое упускаете...")
                .setCallbackQueryId(query.id)
                .call(handler)
        Methods.editMessageText()
                .setChatId(message.chatId)
                .setText("Пользователь " + query.from.firstName + " отказался от брака :(")
                .setReplyMarkup(null)
                .setMessageId(message.messageId)
                .call(handler)
    }

    fun closeMenu(query: CallbackQuery) {
        Methods.editMessageText()
                .setChatId(query.message.chatId)
                .setMessageId(query.message.messageId)
                .setText("Меню закрыто")
                .setReplyMarkup(null)
                .call(handler)
    }

    enum class CAKE_ACTIONS {
        CAKE_OK, CAKE_NOT
    }

}