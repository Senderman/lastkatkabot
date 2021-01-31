package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class Critical(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forAllAdmins: Boolean
        get() = true
    override val desc: String
        get() = "очистка ОЗУ бота от мусора типа незакрытых дуэлей, etc"
    override val command: String
        get() = "/critical"

    override fun execute(message: Message) {
        handler.duels.clear()
        handler.sendMessage(message.chatId, "✅ Все неначатые дуэли были очищены!")
    }
}