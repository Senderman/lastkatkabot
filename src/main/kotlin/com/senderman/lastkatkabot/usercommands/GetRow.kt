package com.senderman.lastkatkabot.usercommands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class GetRow(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/getrow"
    override val desc: String
        get() = "показать сообщение с рассчетом юзеров"

    override fun execute(message: Message) {
        if (!message.isGroupMessage && !message.isSuperGroupMessage) return
        if (message.chatId !in handler.userRows) {
            handler.sendMessage(message.chatId, "У вас пока еще нет списка!")
            return
        }

        try {
            handler.execute(
                SendMessage(message.chatId, "Вот!")
                    .setReplyToMessageId(handler.userRows[message.chatId]!!.messageId)
            )
        } catch (e: TelegramApiException) {
            handler.sendMessage(message.chatId, "У вас пока еще нет списка!")
        }
    }
}