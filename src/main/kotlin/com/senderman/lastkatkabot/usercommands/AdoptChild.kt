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

class AdoptChild(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/adoptchild"
    override val desc: String
        get() = "(reply) усыновить ребенка. Требуется согласие обоих супругов."

    override fun execute(message: Message) {
        if (!message.isReply || message.isUserMessage) return

        val fatherId = message.from.id
        val motherId = Services.db.getLover(fatherId)
        if (motherId == 0) {
            handler.sendMessage(message.chatId, "Вы не можете усыновить ребенка, если у вас нет второй половинки!")
            return
        }

        val father = TgUser(message.from)
        val mother = TgUser(Methods.getChatMember(message.chatId, motherId).call(handler).user)
        val child = TgUser(message.replyToMessage.from)
        if (child.id == motherId || child.id == fatherId) return

        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(
            listOf(
                InlineKeyboardButton().apply {
                    text = "Усыновить"
                    callbackData = "${Callbacks.ADOPT_CHILD} ${child.id} $motherId"
                },
                InlineKeyboardButton().apply {
                    text = "Отказаться"
                    callbackData = "${Callbacks.DECLINE_CHILD} ${child.id} $motherId"
                }
            ))

        //Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        Methods.sendMessage()
            .setChatId(message.chatId)
            .setText(
                "${mother.link}, ${father.link} предлагает вам усыновить ${child.link}! (ребёнок навсегда останется у обоих)"
            )
            .setReplyToMessageId(message.replyToMessage.messageId)
            .setReplyMarkup(markup)
            .enableHtml()
            .call(handler)
    }
}
