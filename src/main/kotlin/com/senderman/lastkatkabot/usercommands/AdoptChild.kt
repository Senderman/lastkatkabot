package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.Callbacks
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class AdoptChild(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/adoptchild"
    override val desc: String
        get() = "(reply) усыновить ребенка. Требуется согласие обоих супругов."

    override fun execute(message: Message) {
        if (!message.isReply || message.isUserMessage) return
        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(listOf(
            InlineKeyboardButton().apply {
                text = "Принять"
                callbackData = Callbacks.CALLBACK_CAKE_OK + message.text
                    .replace("/cake", "")
            },
            InlineKeyboardButton().apply {
                text = "Отказаться"
                callbackData = Callbacks.CALLBACK_CAKE_NOT + message.text
                    .replace("/cake", "")
            }
        ))
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        val adult = TgUser(message.from)
        val child = TgUser(message.replyToMessage.from)
        handler.sendMessage(
            Methods.sendMessage()
                .setChatId(message.chatId)
                .setText(
                    "\uD83C\uDF82 ${luckyOne.name} пользователь ${presenter.name} подарил вам тортик " +
                            message.text.replace("/cake", "")
                )
                .setReplyToMessageId(message.replyToMessage.messageId)
                .setReplyMarkup(markup)
