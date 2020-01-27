package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class Action(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val command: String
        get() = "/action"
    override val desc: String
        get() = "сделать действие. Действие указывать через пробел, можно реплаем"

    override fun execute(message: Message) {
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        if (message.text.split("\\s+".toRegex()).size == 1) return

        val action = message.text.split("\\s+".toRegex(), 2)[1]
        val sm = Methods.sendMessage(message.chatId, message.from.firstName + " " + action)
        if (message.isReply) sm.replyToMessageId = message.replyToMessage.messageId
        try {
            handler.sendMessage(sm)
        } catch (ignored: Exception) {
        }
    }
}