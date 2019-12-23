package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class PayRespects(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/f"
    override val desc: String
        get() = "(reply) press f to pay respects. А можно вот так: /f штаны за 40 хривень"

    override fun execute(message: Message) {
        if (message.isUserMessage) return
        if (message.isReply && message.from.firstName == message.replyToMessage.from.firstName) return

        val `object` = if (message.text.split(" ").size > 1)
            message.text.split(" ".toRegex(), 2)[1]
        else
            message.replyToMessage.from.firstName

        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        val text = "\uD83D\uDD6F Press F to pay respects to $`object`" +
                "\n${message.from.firstName} has payed respects"
        handler.sendMessage(
            Methods.sendMessage()
                .setChatId(message.chatId)
                .setText(text)
                .setReplyMarkup(markupForPayingRespects)
        )
    }

    companion object {
        val markupForPayingRespects: InlineKeyboardMarkup
            get() {
                val markup = InlineKeyboardMarkup()
                markup.keyboard = listOf(
                    listOf(
                        InlineKeyboardButton()
                            .setText("F")
                            .setCallbackData(LastkatkaBot.CALLBACK_PAY_RESPECTS)
                    )
                )
                return markup
            }
    }
}