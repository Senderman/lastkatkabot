package com.senderman.lastkatkabot.callbacks

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class DeletePremium(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.DELETE_PREM

    override fun handle(query: CallbackQuery) {
        val userId = query.getCleanData().toInt()
        Services.db.removeTGUser(userId, DBService.UserType.PREMIUM)
        handler.premiumUsers.remove(userId)
        answerQuery(query, "Пользователь удален из списка")
        handler.sendMessage(userId, "Разработчик удалил вас из премиум пользователей бота!")
        Methods.deleteMessage(query.message.chatId, query.message.messageId).call(handler)
    }
}