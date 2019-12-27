package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.logging.BotLogger

class Announce(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forMainAdmin: Boolean
        get() = true
    override val command: String
        get() = "/announce"
    override val desc: String
        get() = "рассылка сообщения всем в личку"


    override fun execute(message: Message) {
        handler.sendMessage(message.chatId, "✅ Рассылка запущена!")
        var text = message.text
        text = "\uD83D\uDCE3 <b>Объявление</b>\n\n" + text.split("\\s+".toRegex(), 2)[1]
        val usersIds = Services.db.getAllUsersIds()
        var counter = 0
        Thread {
            for (userId in usersIds) {
                try {
                    handler.execute(SendMessage(userId.toLong(), text).enableHtml(true))
                    counter++
                } catch (e: TelegramApiException) {
                    BotLogger.error("ANNOUNCE", e.toString())
                }
            }
        }.start()
        handler.sendMessage(message.chatId, "Объявление получили $counter/${usersIds.size} человек")
    }
}


        
