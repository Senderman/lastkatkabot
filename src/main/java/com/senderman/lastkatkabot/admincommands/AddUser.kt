package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message

class AddUser constructor(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val command: String
        get() = ""
    override val desc: String
        get() = ""

    override fun execute(message: Message) {
        if (!message.isReply) return
        val id = message.replyToMessage.from.id
        val list: MutableSet<Int>
        val format: String
        when (type) {
            UserType.ADMINS -> {
                list = handler.admins
                format = "✅ %1\$s теперь мой хозяин!"
            }
            UserType.BLACKLIST -> {
                if (handler.isFromAdmin(message.replyToMessage) || handler.isPremiumUser(message.replyToMessage)) {
                    handler.sendMessage(message.chatId, "Мы таких в плохие киси не берем!")
                    return
                }
                list = handler.blacklist
                format = "\uD83D\uDE3E %1\$s - плохая киса!"
            }
            UserType.PREMIUM -> {
                list = handler.premiumUsers
                format = "\uD83D\uDC51 %1\$s теперь премиум пользователь!"
            }
    }
}
