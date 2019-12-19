package com.senderman.lastkatkabot.admincommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message

class Owners constructor(val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forAllAdmins: Boolean
        get() = true
    override val desc: String
        get() = "управление/просмотр админами бота. Управление доступно только главному админу в лс"
    override val command: String
        get() = "/owners"

    override fun execute(message: Message) {
        UserLister.listUsers(handler, message, DBService.UserType.ADMINS)
    }
}