package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class BlockUser(private val handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.BLOCK_USER

    override fun handle(query: CallbackQuery) {
        val userId = query.getCleanData().toInt()
        if (userId in handler.admins) {
            answerQuery(query, "Но это же админ!")
            return
        }
        handler.blacklist.add(userId)
        Services.db.addTgUser(userId, DBService.UserType.BLACKLIST)
        answerQuery(query, "Пользователь заблокирован!", false)
        handler.deleteMessage(query.message.chatId, query.message.messageId)
        handler.sendMessage(userId, "\uD83D\uDE3E Разработчик добавил вас в черный список бота!")
    }
}