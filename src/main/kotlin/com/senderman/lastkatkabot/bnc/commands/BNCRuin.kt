package com.senderman.lastkatkabot.bnc.commands

import com.senderman.neblib.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message

class BNCRuin (private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/bncruin"
    override val desc: String
        get() = "вкл/выкл антируин"
    override val showInHelp: Boolean
        get() = false

    override fun execute(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.changeAntiRuin()
    }
}