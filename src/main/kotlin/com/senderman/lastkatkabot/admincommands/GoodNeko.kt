package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.Message

class GoodNeko(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forAllAdmins: Boolean
        get() = true
    override val command: String
        get() = "/goodneko"
    override val desc: String
        get() = "(reply) убрать юзера из чс бота"

    override fun execute(message: Message) {
        if (!message.isReply) return
        val neko = TgUser(message.replyToMessage.from.id, message.replyToMessage.from.firstName)
        Services.db.removeTGUser(neko.id, DBService.UserType.BLACKLIST)
        handler.blacklist.remove(message.replyToMessage.from.id)
        handler.sendMessage(message.chatId, "\uD83D\uDE38 ${neko.link} - хорошая киса!")
    }
}


        
