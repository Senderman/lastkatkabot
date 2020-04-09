package com.senderman.lastkatkabot

import com.senderman.lastkatkabot.bnc.VoteBnc
import com.senderman.lastkatkabot.callbacks.*
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class CallbackHandlersKeeper(val handler: LastkatkaBotHandler) {
    private val callbacks: MutableMap<String, CallbackHandler> = HashMap()

    init {
        register(AcceptMarriage(handler))
        register(AnswerFeedback(handler))
        register(BlockUser(handler))
        register(CakeNot(handler))
        register(CakeOk(handler))
        register(CloseMenu(handler))
        register(DeclineChild(handler))
        register(DeleteAdmin(handler))
        register(DeleteNeko(handler))
        register(DeletePremium(handler))
        register(DenyMarriage(handler))
        register(JoinDuel(handler))
        register(VoteBnc(handler))
        register(PayRespects(handler))
    }

    private fun register(ch: CallbackHandler) {
        callbacks[ch.trigger] = ch
    }

    fun findHandler(query: CallbackQuery): CallbackHandler? = callbacks[query.data.split(" ")[1].trim()]
}