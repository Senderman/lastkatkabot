package com.senderman.lastkatkabot.tempobjects

import com.senderman.lastkatkabot.Services
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import java.util.*

class UserRow(message: Message) {
    private val maxUsers = 50
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
        if (divider <= 0) throw Exception("Неположительное число")
        checkedUsers = HashSet()
        messageText = "<b>$title:</b>\n\n"
        val resultMessage = Services.handler.sendMessage(message.chatId, messageText)
        messageId = resultMessage.messageId
        Services.db.saveRow(chatId, this)
    }

    fun addUser(newUser: User) {
        if (newUser.id in checkedUsers) return
        if (checkedUsers.size +1 > maxUsers) {
            deleteRow()
            return
        }
        
        val user = TgUser(newUser)
        checkedUsers.add(user.id)
        val pref = if (checkedUsers.size % divider == 0) "" else "не"
        messageText += "${checkedUsers.size}. ${user.link} - $pref $name\n"
        try {
            Services.handler.execute(
                EditMessageText()
                    .setChatId(chatId)
                    .setMessageId(messageId)
                    .setText(messageText)
                    .enableHtml(true)
            )
        } catch (e: TelegramApiRequestException) {
            deleteRow()
        }
        Services.db.saveRow(chatId, this)
    }

    private fun deleteRow(){
        Services.handler.userRows.remove(chatId)
        Services.db.deleteRow(chatId)
    }
}