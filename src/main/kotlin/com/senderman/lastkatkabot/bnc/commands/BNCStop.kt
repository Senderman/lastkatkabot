package com.senderman.lastkatkabot.bnc.commands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class BNCStop(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/bncstop"
    override val desc: String
        get() = "остановить текущую игру"
    override val showInHelp: Boolean
        get() = false

    override fun execute(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.createStopPoll(message)
    }
}