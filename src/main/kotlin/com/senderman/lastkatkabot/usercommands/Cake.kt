package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class Cake (private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/cake"
    override val desc: String
        get() = "(reply) подарить тортик. Можно указать начинку, напр. /cake с вишней"

    override fun execute(message: Message) {
        if (message.isUserMessage) return
        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(listOf(
            InlineKeyboardButton().apply {
                text = "Принять"
                callbackData = LastkatkaBot.CALLBACK_CAKE_OK + message.text
                    .replace("/cake", "")
            },
            InlineKeyboardButton().apply {
                text = "Отказаться"
                callbackData = LastkatkaBot.CALLBACK_CAKE_NOT + message.text
                    .replace("/cake", "")
            }
        ))

        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        val presenter = TgUser(message.from)
        val luckyOne = TgUser(message.replyToMessage.from)
        handler.sendMessage(
            Methods.sendMessage()
                .setChatId(message.chatId)
                .setText(
                    "\uD83C\uDF82 ${luckyOne.name} пользователь ${presenter.name} подарил вам тортик " +
                            message.text.replace("/cake", "")
                )
                .setReplyToMessageId(message.replyToMessage.messageId)
                .setReplyMarkup(markup)
        )
    }
}