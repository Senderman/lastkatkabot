package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class RegMe(
    private val handler: LastkatkaBotHandler,
    private val db: DBService
) : CommandExecutor {

    override val command: String = "/regme"

    override val desc: String = "зарегистрироваться на пару дня"

    override fun execute(message: Message) {
        val chatId = message.chatId
        if (message.isUserMessage || message.isChannelMessage) {
            handler.sendMessage(chatId, "Команда недоступна в лс!")
            return
        }

        db.addUserToChat(chatId, message.from.id, message.date)
        handler.sendMessage(
            Methods.sendMessage(chatId, "Бот запомнил вас на 2 месяца!")
                .setReplyToMessageId(message.messageId)
        )
    }
}