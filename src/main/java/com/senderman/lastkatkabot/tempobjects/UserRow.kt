package com.senderman.lastkatkabot.tempobjects

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.TgUser
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.*

class UserRow(message: Message) {
    private val divider: Int
    private val name: String
    private val chatId: Long
    val messageId: Int
    private val checkedUsers: MutableSet<Int>
    private var messageText: String

    init {
        val lines = message.text.trim().split("\n")
        if (lines.size != 3) throw Exception("Неверный формат")
        chatId = message.chatId
        val title = lines[0].split(" ".toRegex(), 2)[1]
        name = lines[1]
        divider = lines[2].toInt()
        checkedUsers = HashSet()
        messageText = "<b>$title:</b>\n\n"
        val resultMessage = Services.handler().sendMessage(message.chatId, messageText)
        messageId = resultMessage.messageId
        Services.db().saveRow(chatId, this)
    }

    fun addUser(message: Message) {
        val user = TgUser(message.from)
        if (checkedUsers.contains(user.id)) return
        checkedUsers.add(user.id)
        messageText += if (checkedUsers.size % divider == 0)
            checkedUsers.size.toString() + ". ${user.getLink()} - $name!\n"
        else
            checkedUsers.size.toString() + ". ${user.getLink()} - не $name\n"
        updateMessage()
    }

    private fun updateMessage() {
        Methods.editMessageText()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setText(messageText)
                .setParseMode(ParseMode.HTML)
                .call(Services.handler())
        Services.db().saveRow(chatId, this)
    }
}