package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import kotlin.concurrent.thread

class Update(private val handler: LastkatkaBotHandler) : CommandExecutor {

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
            update.appendln("* ${params[i]}")
        }
        val chats = Services.db.getChatIdsSet()
        chats.remove(Services.botConfig.tourgroup)
        var counter = 0
        handler.sendMessage(message.chatId, "Рассылка...")
        thread {
            for (chat in chats) {
                try {
                    handler.execute(
                        SendMessage(chat, update.toString()).enableHtml(true)
                    )
                    counter++
                } catch (ignored: TelegramApiException) {
                }
            }
            handler.sendMessage(message.chatId, "Обновления получили $counter/${chats.size} чатов")
        }
    }
}


        
