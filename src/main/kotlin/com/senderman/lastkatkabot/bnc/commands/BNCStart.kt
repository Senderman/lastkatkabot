package com.senderman.lastkatkabot.bnc.commands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.bnc.BullsAndCowsGame
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class BNCStart(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/bnc"
    override val desc: String
        get() = "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа"

    override fun execute(message: Message) {
        if (message.chatId !in handler.bullsAndCowsGames)
            handler.bullsAndCowsGames[message.chatId] = BullsAndCowsGame(message)
        else
            handler.bullsAndCowsGames[message.chatId]?.gameMessage(message.chatId, "В этом чате игра уже идет!")
    }
}