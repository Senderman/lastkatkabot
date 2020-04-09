package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class DenyMarriage(handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.DENY_MARRIAGE

    override fun handle(query: CallbackQuery) {
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        answerQuery(query, "Такое упускаете...")
        editText(query, "Пользователь ${query.from.firstName} отказался от брака :(")
    }

    private fun notFor(query: CallbackQuery): Boolean = query.from.id != query.message.replyToMessage.from.id
}