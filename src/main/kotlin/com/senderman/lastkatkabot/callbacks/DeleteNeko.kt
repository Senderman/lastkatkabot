package com.senderman.lastkatkabot.callbacks

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class DeleteNeko(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.DELETE_NEKO

    override fun handle(query: CallbackQuery) {
        val userId = query.getCleanData().toInt()
        Services.db.removeTGUser(userId, DBService.UserType.BLACKLIST)
        handler.blacklist.remove(userId)
        answerQuery(query, "Пользователь удален из списка")
        handler.sendMessage(userId, "Разработчик удалил вас из черного списка бота!")
        Methods.deleteMessage(query.message.chatId, query.message.messageId).call(handler)
    }
}