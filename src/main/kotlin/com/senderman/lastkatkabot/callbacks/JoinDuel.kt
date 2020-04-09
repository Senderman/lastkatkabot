package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.tempobjects.Duel
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class JoinDuel(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.JOIN_DUEL

    override fun handle(query: CallbackQuery) {
        val message = query.message
        val duel = handler.duels[message.chatId.toString() + " " + message.messageId]
        if (duel == null) {
            Duel.answerCallbackQuery(query, "⏰ Дуэль устарела!", true)
            return
        }
        duel.join(query)
    }
}