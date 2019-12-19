package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message

class Announce constructor(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val command: String
        get() = "/setuphelp"
    override val desc: String
        get() = "инфо о команде /setup"


    override fun execute(message: Message) {
        handler.sendMessage(message.chatId, Services.botConfig.setupHelp)
    }
}


        
