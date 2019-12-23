package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class TransferStats(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forMainAdmin: Boolean
        get() = true
    override val command: String
        get() = "/transfer"
    override val desc: String
        get() = "перенос стат юзера. /transfer fromId toId"

    override fun execute(message: Message) {
        val chatId = message.chatId
        val params = message.text.split("\\s+".toRegex())
        if (params.size != 3) {
            handler.sendMessage(chatId, "Неверное кол-во аргументов!")
            return
        }
        val fromId: Int
        val toId: Int
        try {
            fromId = params[1].toInt()
            toId = params[2].toInt()
        } catch (e: NumberFormatException) {
            handler.sendMessage(chatId, "Неверный формат!")
            return
        }
        handler.sendMessage(chatId, "Переносим данные...")
        Services.db.transferStats(fromId, toId)
        handler.sendMessage(chatId, "Данные успешно перенесены!")
        handler.sendMessage(
            fromId,
            "⚠️ Ваши данные были перенесены на <a href=\"tg://user?id=$toId\">этот</a> аккаунт!"
        )
        handler.sendMessage(
            toId,
            "⚠️ Ваши статы были перенесены с <a href=\"tg://user?id=$fromId\">этого</a> аккаунта!"
        )
    }
}


        
