package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class Owner(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forMainAdmin: Boolean
        get() = true
    override val desc: String
        get() = "(reply) добавить админа бота"
    override val command: String
        get() = "/owner"

    override fun execute(message: Message) {
        if (!message.isReply) return

        UserAdder.addUser(handler, message, DBService.UserType.ADMINS)
    }
}