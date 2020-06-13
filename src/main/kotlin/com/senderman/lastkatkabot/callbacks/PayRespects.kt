package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.usercommands.PayRespects
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class PayRespects(handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.PAY_RESPECTS

    override fun handle(query: CallbackQuery) {
        if (query.from.firstName in query.message.text) {
            answerQuery(query, "You've already payed respects! (or you've tried to pay respects to yourself)")
            return
        }
        answerQuery(query, "You've payed respects")
        editText(
            query,
            "${query.message.text}\n${query.from.firstName} has payed respects",
            query.message.replyMarkup
        )
    }
}