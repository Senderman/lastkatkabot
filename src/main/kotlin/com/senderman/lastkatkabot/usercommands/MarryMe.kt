package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.callbacks.Callbacks
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class MarryMe(private val handler: LastkatkaBotHandler, private val db: DBService) : CommandExecutor {
    override val command: String
        get() = "/marryme"
    override val desc: String
        get() = "(reply) пожениться на ком-нибудь"

    override fun execute(message: Message) {
        if (message.isUserMessage || !message.isReply) return
        if (message.from.id == message.replyToMessage.from.id) {
            handler.sendMessage(message.chatId, "На самом себе нельзя жениться!!!")
            return
        }
        val chatId = message.chatId
        val userId = message.from.id
        if (db.getLover(userId) != 0) {
            handler.sendMessage(chatId, "Всмысле? Вы что, хотите изменить своей второй половинке?!")
            return
        }
        val loverId: Int = message.replyToMessage.from.id
        if (db.getLover(loverId) != 0) {
            handler.sendMessage(chatId, "У этого пользователя уже есть своя вторая половинка!")
            return
        }

        val user = TgUser(Methods.getChatMember(chatId, userId).call(handler).user)
        val text = "Пользователь ${user.link} предлагает вам руку, сердце и шавуху. Вы согласны?"
        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(listOf(
            InlineKeyboardButton().apply {
                this.text = "Принять"
                callbackData = Callbacks.ACCEPT_MARRIAGE + "$userId"

            },
            InlineKeyboardButton().apply {
                this.text = "Отказаться"
                callbackData = Callbacks.DENY_MARRIAGE
            }
        ))
        val sm = Methods.sendMessage()
            .setChatId(chatId)
            .setText(text)
            .setReplyToMessageId(message.replyToMessage.messageId)
            .setReplyMarkup(markup)
            .setParseMode(ParseMode.HTML)
        handler.sendMessage(sm)
    }
}