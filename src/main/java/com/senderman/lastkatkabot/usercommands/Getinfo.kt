package com.senderman.lastkatkabot.usercommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message

class Getinfo constructor(val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/getinfo"
    override val desc: String
        get() = "(reply) инфа о сообщении в формате JSON"

    override fun execute(message: Message) {
        if (!message.isReply) return

        val replacements = mapOf(
                "[ ,]*\\w+='?null'?" to "",
                "(\\w*[iI]d=)(-?\\d+)" to "$1<code>$2</code>",
                "([{,])" to "$1\n",
                "(})" to "\n$1",
                "(=)" to " $1 "
        )
        var text = message.replyToMessage.toString()
        for ((old, new) in replacements) text = text.replace(old.toRegex(), new)
        handler.sendMessage(message.chatId, text)
    }
}