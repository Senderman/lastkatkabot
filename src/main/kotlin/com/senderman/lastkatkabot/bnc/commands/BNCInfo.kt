package com.senderman.lastkatkabot.bnc.commands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class BNCInfo(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/bncinfo"
    override val desc: String
        get() = "информация о текущей игре"
    override val showInHelp: Boolean
        get() = false

    override fun execute(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.sendGameInfo(message)
    }
}