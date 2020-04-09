package com.senderman.lastkatkabot.callbacks

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class DeleteAdmin(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.DELETE_ADMIN

    override fun handle(query: CallbackQuery) {
        val userId = query.getCleanData().toInt()
        Services.db.removeTGUser(userId, DBService.UserType.ADMINS)
        handler.admins.remove(userId)
        answerQuery(query, "Пользователь удален из списка")
        handler.sendMessage(userId, "Разработчик удалил вас из админов бота!")
        Methods.deleteMessage(query.message.chatId, query.message.messageId).call(handler)
    }
}