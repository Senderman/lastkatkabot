package com.senderman.lastkatkabot.admincommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message

class Update (private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val command: String
        get() = "/update"
    override val desc: String
        get() = "рассылка информации по обновлениям в чаты. Обновления писать построчно"
    override val forMainAdmin: Boolean
        get() = true

    override fun execute(message: Message) {
        val params = message.text.split("\n")
        if (params.size < 2) {
            handler.sendMessage(message.chatId, "Неверное количество аргументов!")
            return
        }
        val update = StringBuilder().append("\uD83D\uDCE3 <b>ВАЖНОЕ ОБНОВЛЕНИЕ:</b> \n\n")
        for (i in 1 until params.size) {
            update.append("* ${params[i]}\n")
        }
        val chats = Services.db.getChatIdsSet()
        chats.remove(Services.botConfig.tourgroup)
        for (chat in chats) {
            handler.sendMessage(chat, update.toString())
        }
    }
}


        
