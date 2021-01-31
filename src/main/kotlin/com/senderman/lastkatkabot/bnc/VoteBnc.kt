package com.senderman.lastkatkabot.bnc

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.callbacks.CallbackHandler
import com.senderman.lastkatkabot.callbacks.Callbacks
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class VoteBnc(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.VOTE_BNC

    override fun handle(query: CallbackQuery) {
        handler.bullsAndCowsGames[query.message.chatId]?.addVote(query)
    }
}