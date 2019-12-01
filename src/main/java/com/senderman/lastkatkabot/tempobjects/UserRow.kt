package com.senderman.lastkatkabot.tempobjects

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.TgUser
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
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
        val resultMessage = Services.handler.sendMessage(message.chatId, messageText)
        messageId = resultMessage.messageId
        Services.db.saveRow(chatId, this)
    }

    fun addUser(newUser: User) {
        if (newUser.id in checkedUsers) return
        val user = TgUser(newUser)
        checkedUsers.add(user.id)
        val pref = if (checkedUsers.size % divider == 0) "" else "не"
        messageText += "${checkedUsers.size}. ${user.getLink()} - $pref $name\n"
        Methods.editMessageText()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setText(messageText)
                .enableHtml()
                .call(Services.handler)
        Services.db.saveRow(chatId, this)
    }
}