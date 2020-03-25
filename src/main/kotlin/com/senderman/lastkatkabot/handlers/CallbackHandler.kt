package com.senderman.lastkatkabot.handlers

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.Callbacks
import com.senderman.lastkatkabot.DBService.UserType
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.lastkatkabot.usercommands.PayRespects
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

class CallbackHandler(private val handler: LastkatkaBotHandler) {

    private fun answerQuery(query: CallbackQuery, text: String, showAlert: Boolean = true) {
        Methods.answerCallbackQuery()
            .setCallbackQueryId(query.id)
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
        if (query.from.firstName in query.message.text) {
            answerQuery(query, "You've already payed respects! (or you've tried to pay respects to yourself)")
            return
        }
        answerQuery(query, "You've payed respects")
        editText(
            query,
            "${query.message.text}\n${query.from.firstName} has payed respects",
            PayRespects.markupForPayingRespects
        )
    }

    fun cake(query: CallbackQuery, action: CakeAcion) {
        if (query.from.id != query.message.replyToMessage.from.id) {
            answerQuery(query, "Этот тортик не вам!")
            return
        }
        if (query.message.date + 2400 < System.currentTimeMillis() / 1000) {
            answerQuery(query, "Тортик испортился!")
            editText(query, "\uD83E\uDD22 Тортик попытались взять, но он испортился!")
            return
        }
        val emt = Methods.editMessageText()
            .setChatId(query.message.chatId)
            .setMessageId(query.message.messageId)
            .setReplyMarkup(null)
        when (action) {
            CakeAcion.CAKE_OK -> {
                emt.setText(
                    "\uD83C\uDF82 ${query.from.firstName} принял тортик"
                            + query.data.replace(Callbacks.CALLBACK_CAKE_OK, "")
                )
            }

            CakeAcion.CAKE_NOT -> {
                answerQuery(query, "Ну и ладно", false)
                emt.setText(
                    "\uD83D\uDEAB \uD83C\uDF82 ${query.from.firstName} отказался от тортика"
                            + query.data.replace(Callbacks.CALLBACK_CAKE_NOT, "")
                )
            }
        }
        emt.call(handler)
    }

    // TODO implement
    /*fun registerInTournament(query: CallbackQuery) {
        val memberId = query.from.id
        if (!handler.tournamentHandler.isEnabled) {
            answerQuery(query, "⚠️ На данный момент нет открытых раундов!")
            return
        }
        if (memberId in handler.tournamentHandler.membersIds) {
            answerQuery(query, "⚠️ Вы уже получили разрешение на отправку сообщений!")
            return
        }
        if (query.from.userName !in handler.tournamentHandler.members) {
            answerQuery(query, "\uD83D\uDEAB Вы не являетесь участником текущего раунда!")
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
        answerQuery(query, "✅ Вам даны права на отправку сообщений в группе турнира!")
        handler.sendMessage(Services.botConfig.tourgroup, "✅ ${query.from.firstName} получил доступ к игре!")
    }*/

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
        answerQuery(query, "Пользователь удален из списка")
        handler.sendMessage(userId, "Разработчик удалил вас из $listName бота!")
        Methods.deleteMessage(query.message.chatId, query.message.messageId).call(handler)
    }

    fun blockUser(query: CallbackQuery) {
        val userId = query.data.split(" ")[1].toInt()
        if (userId in handler.admins) {
            answerQuery(query, "Но это же админ!")
            return
        }
        handler.blacklist.add(userId)
        Services.db.addTgUser(userId, UserType.BLACKLIST)
        answerQuery(query, "Пользователь заблокирован!", false)
        Methods.deleteMessage(query.message.chatId, query.message.messageId)
        handler.sendMessage(userId, "\uD83D\uDE3E Разработчик добавил вас в черный список бота!")
    }

    fun answerFeedback(query: CallbackQuery) {
        val params = query.data.split(" ")
        Services.handler.feedbackChatId = params[1].toLong()
        Services.handler.feedbackMessageId = params[2].toInt()
        answerQuery(query, "Введите ответ", false)
        handler.sendMessage(Services.botConfig.mainAdmin, "Введите ответ")
    }

    private fun notFor(query: CallbackQuery): Boolean {
        val userId = query.from.id
        val loverId = query.data.split(" ")[2].toInt()
        return userId != loverId
    }

    fun acceptMarriage(query: CallbackQuery) {
        val userId = query.from.id
        val message = query.message
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        if (Services.db.getLover(userId) != 0) {
            answerQuery(query, "У вас уже есть вторая половинка!")
            return
        }
        val coupleId = query.data.split(" ")[1].toInt()
        if (Services.db.getLover(coupleId) != 0) {
            answerQuery(query, "Слишком поздно! У пользователя уже есть другой!")
            return
        }
        answerQuery(query, "Поздравляем! Теперь у вас есть вторая половинка")
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        // user - acceptor, couple - inviter
        val user = TgUser(userId, query.from.firstName)
        val couple = TgUser(Methods.getChatMember(message.chatId, coupleId).call(handler).user)
        Services.db.setLover(user.id, couple.id)
        handler.execute(
            SendMessage(
                couple.id.toLong(),
                "Поздравляем! Теперь ваша вторая половинка - " + user.link
            )
                .enableHtml(true)
        )
        val format =
            "Внимание все! Сегодня великий день свадьбы %s и %s! Так давайте же поздравим их и съедим шавуху в часть такого праздника!"
        val text = String.format(format, user.link, couple.link)
        handler.sendMessage(message.chatId, text)
    }

    fun denyMarriage(query: CallbackQuery) {
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        answerQuery(query, "Такое упускаете...")
        editText(query, "Пользователь ${query.from.firstName} отказался от брака :(")
    }

    fun declineChild(query: CallbackQuery) {
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        answerQuery(query, "Такое упускаете...")
        editText(query, "Пользователь ${query.from.firstName} отказался от ребенка :(")
    }

    fun acceptChild(query: CallbackQuery) {
        val userId = query.from.id
        val message = query.message
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        if (Services.db.getChild(userId) != 0) {
            answerQuery(query, "У вас уже есть ребенок!")
            return
        }
        val childId = query.data.split(" ")[1].toInt()
        answerQuery(query, "Поздравляем! Теперь у вас ребенок")
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        // user - acceptor, couple - inviter
        val user = TgUser(userId, query.from.firstName)
        val child = TgUser(Methods.getChatMember(message.chatId, childId).call(handler).user)
        val lover = TgUser(Methods.getChatMember(message.chatId, Services.db.getLover(userId)).call(handler).user)
        Services.db.setChild(user.id, lover.id, child.id)
        handler.sendMessage(
            child.id.toLong(),
            "Поздравляем! Теперь ваши родители - ${user.link} и его супруг(а) ${lover.link}!"
        )
        val text =
            "Внимание все! Сегодня великий день усыновления ${user.link} ребенка ${child.link}! Так давайте же поздравим их и съедим шавуху в часть такого праздника!"
        handler.sendMessage(message.chatId, text)
    }

    fun closeMenu(query: CallbackQuery) = editText(query, "Меню закрыто")

    enum class CakeAcion {
        CAKE_OK, CAKE_NOT
    }

}