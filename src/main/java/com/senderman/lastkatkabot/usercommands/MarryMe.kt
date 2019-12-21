package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.TgUser
import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class MarryMe constructor(val handler: LastkatkaBotHandler): CommandExecutor {
    override val command: String
        get() ="/marryme"
    override val desc: String
        get() = "(reply) пожениться на ком-нибудь"

    override fun execute(message: Message) {
        val marryById = message.text.trim().matches(Regex("/marryme\\s+\\d+"))
        val chatId = message.chatId
        val userId = message.from.id
        val text: String
        val loverId: Int
        if (Services.db.getLover(userId) != 0) {
            handler.sendMessage(chatId, "Всмысле? Вы что, хотите изменить своей второй половинке?!")
            return
        }

        if (!marryById) {
            if (!message.isReply
                    || message.from.id == message.replyToMessage.from.id || message.replyToMessage.from.bot) return
            loverId = message.replyToMessage.from.id
            val user = TgUser(Methods.getChatMember(chatId, userId).call(handler).user)
            text = "Пользователь " + user.link + " предлагает вам руку, сердце и шавуху. Вы согласны?"

        } else {
            if (message.isUserMessage) return
            loverId = try {
                message.text.split(" ")[1].toInt()
            } catch (e: NumberFormatException) {
                handler.sendMessage(chatId, "Неверный формат!")
                return
            }
            val user = TgUser(Methods.getChatMember(chatId, userId).call(handler).user)
            val lover = TgUser(Methods.getChatMember(chatId, loverId).call(handler).user)
            text = "${lover.link}, пользователь ${user.link} предлагает вам руку, сердце и шавуху. Вы согласны?"
        }
        if (Services.db.getLover(loverId) != 0) {
            handler.sendMessage(chatId, "У этого пользователя уже есть своя вторая половинка!")
            return
        }
        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(listOf(
                InlineKeyboardButton().apply {
                    this.text = "Принять"
                    callbackData = LastkatkaBot.CALLBACK_ACCEPT_MARRIAGE + "$userId $loverId"

                },
                InlineKeyboardButton().apply {
                    this.text = "Отказаться"
                    callbackData = LastkatkaBot.CALLBACK_DENY_MARRIAGE + "$userId $loverId"
                }
        ))
        val sm = Methods.sendMessage()
                .setChatId(chatId)
                .setText(text)
                .setReplyMarkup(markup)
        if (!marryById) {
            sm.replyToMessageId = message.replyToMessage.messageId
        }
        handler.sendMessage(sm)
    }
}