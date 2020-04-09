package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class CloseMenu(handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.CLOSE_MENU

    override fun handle(query: CallbackQuery) {
        editText(query, "Меню закрыто")
        answerQuery(query, "Меню закрыто", false)
    }
}