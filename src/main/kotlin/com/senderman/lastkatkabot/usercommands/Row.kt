package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.lastkatkabot.tempobjects.UserRow
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class Row(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/row"
    override val desc: String
        get() = """
            рассчет юзеров, например няшек.
            Синтаксис:
            1 строка - /row Список няшек
            2 строка - няшка
            3 строка - 5
            (т.е. няшкой будет каждый пятый
        """.trimIndent()

    override fun execute(message: Message) {
        if (!message.isGroupMessage && !message.isSuperGroupMessage) return
        try {
            handler.userRows[message.chatId] = UserRow(message)
        } catch (e: Exception) {
            handler.sendMessage(message.chatId, "Неверный формат!")
            return
        }
        Methods.deleteMessage(message.chatId, message.messageId).call(Services.handler)
    }
}