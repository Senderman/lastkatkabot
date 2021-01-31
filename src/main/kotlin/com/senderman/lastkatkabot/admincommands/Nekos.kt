package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class Nekos(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forAllAdmins: Boolean
        get() = true
    override val desc: String
        get() = "посмотреть чс бота. В лс работает как управление чс"
    override val command: String
        get() = "/nekos"

    override fun execute(message: Message) {
        UserLister.listUsers(handler, message, DBService.UserType.BLACKLIST)
    }
}