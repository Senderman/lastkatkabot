package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.lastkatkabot.callbacks.Callbacks
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class FeedBack(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/feedback"
    override val desc: String
        get() = "написать разрабу. Что написать, пишите через пробел. Или просто реплайните на то, что отправить"

    override fun execute(message: Message) {
        val report = message
            .text.trim()
            .replace("/feedback(?:@${handler.botUsername})?\\s*".toRegex(), "")
        if (report.isBlank() && !message.isReply) return

        val user = TgUser(message.from)
        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(listOf(
            InlineKeyboardButton().apply {
                text = "Ответить"
                callbackData = "${Callbacks.ANSWER_FEEDBACK}${message.chatId} ${message.messageId}"
            },
            InlineKeyboardButton().apply {
                text = "Заблокировать"
                callbackData = "${Callbacks.BLOCK_USER} ${message.from.id}"
            }
        ))

        val bugreport = ("⚠️ <b>Фидбек</b>\n\n" +
                "От: ${user.link}\n\n$report")
        handler.sendMessage(
            Methods.sendMessage()
                .setChatId(Services.botConfig.mainAdmin.toLong())
                .setText(bugreport)
                .setReplyMarkup(markup)
        )
        if (message.isReply) {
            Methods.forwardMessage(
                Services.botConfig.mainAdmin.toLong(),
                message.replyToMessage.chatId,
                message.replyToMessage.messageId
            ).call(handler)
        }

        handler.sendMessage(
            Methods.sendMessage()
                .setChatId(message.chatId)
                .setText("✅ Отправлено разрабу бота!")
                .setReplyToMessageId(message.messageId)
        )
    }
}