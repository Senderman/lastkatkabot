package com.senderman.lastkatkabot.usercommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.tempobjects.Duel
import org.telegram.telegrambots.meta.api.objects.Message

class DuelStart constructor(val handler:LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/duel"
    override val desc: String
        get() = "начать дуэль (мини-игра на рандом)"

    override fun execute(message: Message) {
        if (message.isUserMessage) return
        val duel = Duel(message)
        handler.duels[duel.duelId] = duel
    }
}