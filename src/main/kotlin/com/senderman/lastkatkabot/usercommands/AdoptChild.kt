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
        TODO("Not implemented")
        if (!message.isReply || message.isUserMessage) return

        val chatId = message.chatId

        val fatherId = message.from.id
        val motherId = Services.db.getLover(fatherId)
        if (motherId == 0) {
            handler.sendMessage(message.chatId, "Вы не можете усыновить ребенка, если у вас нет второй половинки!")
            return
        }

        val father = TgUser(message.from)
        val mother = TgUser(Methods.getChatMember(message.chatId, motherId).call(handler).user)
        val child = TgUser(message.replyToMessage.from)

        if (child.id == mother.id || child.id == father.id) return
        if (Services.db.getChild(mother.id) != 0 || Services.db.getChild(father.id) != 0) {
            handler.sendMessage(chatId, "У вас / вашей половинки уже есть ребенок!")
            return
        }
        /*if (Services.db.hasParent(child.id)) {
            handler.sendMessage(chatId, "У этого ребенка уже есть родители!")
            return
        }*/

        val markup = InlineKeyboardMarkup()
        markup.keyboard = listOf(
            listOf(
                InlineKeyboardButton().apply {
                    text = "Усыновить (10000 монет на оплату проверки)"
                    callbackData = "${Callbacks.ADOPT_CHILD} ${child.id} ${mother.id}"
                },
                InlineKeyboardButton().apply {
                    text = "Отказаться"
                    callbackData = "${Callbacks.DECLINE_CHILD} ${child.id} ${mother.id}"
                }
            ))

        Methods.sendMessage()
            .setChatId(chatId)
            .setText(
                "${mother.link}, ${father.link} предлагает вам усыновить ${child.link}! (ребёнок навсегда останется у обоих)"
            )
            .setReplyMarkup(markup)
            .enableHtml()
            .call(handler)
    }
}
