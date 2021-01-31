package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class AddPremium(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forAllAdmins: Boolean
        get() = true
    override val desc: String
        get() = "(reply) добавить премиум-пользователя"
    override val command: String
        get() = "/addpremium"

    override fun execute(message: Message) {
        if (!message.isReply) return

        UserAdder.addUser(handler, message, DBService.UserType.PREMIUM)
    }
}