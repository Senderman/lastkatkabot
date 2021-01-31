package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class AnswerFeedback(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.ANSWER_FEEDBACK

    override fun handle(query: CallbackQuery) {
        val params = query.getCleanData().split(" ")
        Services.handler.feedbackChatId = params[0].toLong()
        Services.handler.feedbackMessageId = params[1].toInt()
        answerQuery(query, "Введите ответ", false)
        handler.sendMessage(Services.botConfig.mainAdmin, "Введите ответ")
    }
}