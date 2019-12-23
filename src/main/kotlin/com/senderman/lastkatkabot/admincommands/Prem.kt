package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class Prem(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forAllAdmins: Boolean
        get() = true
    override val command: String
        get() = "/prem"
    override val desc: String
        get() = "управление/просмотр премиум-пользователями. Управление доступно только главному админу в лс"

    override fun execute(message: Message) {
        UserLister.listUsers(handler, message, DBService.UserType.PREMIUM)
    }
}