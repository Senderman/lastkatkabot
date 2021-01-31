package com.senderman.lastkatkabot.callbacks

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class AcceptMarriage(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.ACCEPT_MARRIAGE

    override fun handle(query: CallbackQuery) {
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        val userId = query.getCleanData().toInt()
        if (Services.db.getLover(userId) != 0) {
            answerQuery(query, "У вас уже есть вторая половинка!")
            return
        }
        val loverId = query.from.id
        if (Services.db.getLover(loverId) != 0) {
            answerQuery(query, "Слишком поздно! У пользователя уже есть другой!")
            return
        }

        answerQuery(query, "Поздравляем! Теперь у вас есть вторая половинка")
        val message = query.message
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        // user - inviter, lover - acceptor
        val lover = TgUser(query.from)
        val user = TgUser(Methods.getChatMember(message.chatId, userId).call(handler).user)
        Services.db.setLover(user.id, lover.id)
        try {
            handler.execute(
                SendMessage(
                    user.id.toLong(),
                    "Поздравляем! Теперь ваша вторая половинка - " + lover.link
                )
                    .enableHtml(true)
            )
        } catch (ignored: TelegramApiException) {
        }
        val format =
            "Внимание все! Сегодня великий день свадьбы %s и %s! Так давайте же поздравим их и съедим шавуху в часть такого праздника!"
        val text = String.format(format, user.link, lover.link)
        handler.sendMessage(message.chatId, text)
    }

    private fun notFor(query: CallbackQuery): Boolean = query.from.id != query.message.replyToMessage.from.id

}