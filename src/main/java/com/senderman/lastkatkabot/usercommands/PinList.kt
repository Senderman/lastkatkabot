package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message

class PinList constructor(val handler: LastkatkaBotHandler) : CommandExecutor {

    override val command: String
        get() = "/pinlist"
    override val desc: String
        get() = "ответьте этим на сообщение со списком игроков в верфульфа чтобы запинить его"

    override fun execute(message: Message) {
        if (!isFromWwBot(message)) return
        Methods.Administration.pinChatMessage(message.chatId, message.replyToMessage.messageId)
                .setNotificationEnabled(false).call(handler)
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
    }

    companion object {
        private fun isFromWwBot(message: Message): Boolean {
            return message.replyToMessage.from.userName in Services.botConfig.wwBots &&
                    message.replyToMessage.text.startsWith("#players")
        }
    }
}