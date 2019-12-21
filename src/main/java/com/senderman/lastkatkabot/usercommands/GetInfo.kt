package com.senderman.lastkatkabot.usercommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message

class GetInfo constructor(val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/getinfo"
    override val desc: String
        get() = "(reply) инфа о сообщении в формате JSON"

    override fun execute(message: Message) {
        if (!message.isReply) return

        handler.sendMessage(message.chatId, LastkatkaBot.formatJSON(message.replyToMessage.toString()))
    }
}