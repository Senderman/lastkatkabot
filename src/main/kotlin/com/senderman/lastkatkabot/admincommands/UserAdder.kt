package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.Message

class UserAdder {
    companion object {
        fun addUser(handler: LastkatkaBotHandler, message: Message, type: DBService.UserType) {
            if (!message.isReply) return
            val id = message.replyToMessage.from.id
            val list: MutableSet<Int>
            val format: String
            when (type) {
                DBService.UserType.ADMINS -> {
                    list = handler.admins
                    format = "✅ %1\$s теперь мой хозяин!"
                }
                DBService.UserType.BLACKLIST -> {
                    if (handler.isFromAdmin(message.replyToMessage) || handler.isPremiumUser(message.replyToMessage)) {
                        handler.sendMessage(message.chatId, "Мы таких в плохие киси не берем!")
                        return
                    }
                    list = handler.blacklist
                    format = "\uD83D\uDE3E %1\$s - плохая киса!"
                }
                DBService.UserType.PREMIUM -> {
                    list = handler.premiumUsers
                    format = "\uD83D\uDC51 %1\$s теперь премиум пользователь!"
                }
            }

            val name = message.replyToMessage.from.firstName
            val user = TgUser(id, name)
            list.add(id)
            Services.db.addTgUser(id, type)
            handler.sendMessage(message.chatId, String.format(format, user.name))
        }
    }
}