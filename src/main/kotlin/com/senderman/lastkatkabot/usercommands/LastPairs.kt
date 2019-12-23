package com.senderman.lastkatkabot.usercommands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class LastPairs(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/lastpairs"
    override val desc: String
        get() = "последние 10 пар чата"

    override fun execute(message: Message) {
        if (message.isUserMessage) return
        val chatId = message.chatId
        val history = Services.db.getPairsHistory(chatId)
        handler.sendMessage(chatId,
            history?.let {
                "<b>Последние 10 пар:</b>\n\n$it"
            } ?: "В этом чате еще никогда не запускали команду /pair!"
        )
    }
}