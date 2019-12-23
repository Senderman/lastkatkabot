package com.senderman.lastkatkabot.bnc.commands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class BNCHelp(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/bnchelp"
    override val desc: String
        get() = "помощь по игре Быки и Коровы"

    override fun execute(message: Message) {
        val sendPhoto = Methods.sendPhoto()
            .setChatId(message.chatId)
            .setFile(Services.botConfig.bncphoto)
        if (message.isReply) sendPhoto.replyToMessageId =
            message.replyToMessage.messageId else sendPhoto.replyToMessageId = message.messageId
        sendPhoto.call(handler)
    }
}