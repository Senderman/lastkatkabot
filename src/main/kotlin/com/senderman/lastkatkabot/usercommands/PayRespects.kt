package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.callbacks.Callbacks
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
        if (message.from.firstName == message.replyToMessage.from.firstName) return

        val obj: String = when {
            message.text.split(" ").size > 1 -> message.text.split(" ".toRegex(), 2)[1]
            message.isReply -> message.replyToMessage.from.firstName
            else -> return
        }

        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        val text = "\uD83D\uDD6F Press F to pay respects to $obj" +
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
                            .setCallbackData(Callbacks.PAY_RESPECTS)
                    )
                )
                return markup
            }
    }
}