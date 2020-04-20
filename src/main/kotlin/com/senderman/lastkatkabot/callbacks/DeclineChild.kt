package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class DeclineChild(handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.DECLINE_CHILD

    override fun handle(query: CallbackQuery) {
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        answerQuery(query, "Такое упускаете...")
        editText(query, "Пользователь ${query.from.firstName} отказался от ребенка :(")
    }

    private fun notFor(query: CallbackQuery): Boolean {
        val userId = query.from.id
        val loverId = query.getCleanData().split(" ")[1].toInt()
        return userId != loverId
    }
}