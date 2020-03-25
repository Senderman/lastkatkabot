package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.Callbacks
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
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
        val father = TgUser(message.from)
        val fatherId = father.id
        val motherId = Services.db.getLover(fatherId)
        //val mother = TgUser(Methods.getChatMember(message.chatId, motherId).call(handler).user)
        val mother = TgUser(handler.execute(GetChatMember().setChatId(motherId.toLong()).setUserId(motherId)).user)
        if (motherId == 0) {
            handler.sendMessage(message.chatId, "Вы не можете усыновить ребенка, если у вас нет второй половинки!")
            return
        }
        val child = TgUser(message.replyToMessage.from)
        val childId = child.id
        if (childId == motherId || childId == fatherId) return
        
        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(listOf(
            InlineKeyboardButton().apply {
                text = "Усыновить"
                callbackData = "${Callbacks.CALLBACK_ADOPT_CHILD} $childId $motherId"
            },
            InlineKeyboardButton().apply {
                text = "Отказаться"
                callbackData = "${Callbacks.CALLBACK_DECLINE_CHILD} $childId $motherId"
            }
        ))

        //Methods.deleteMessage(message.chatId, message.messageId).call(handler)
        val sm = Methods.sendMessage()
            .setChatId(message.chatId)
            .setText(
                "${mother.link}, ${father.link} предлагает вам усыновить ${child.link}! (ребенок навсегда останется у обоих)"
            )
            .setReplyToMessageId(message.replyToMessage.messageId)
            .setReplyMarkup(markup)
        sm.call(handler)
    }
}
