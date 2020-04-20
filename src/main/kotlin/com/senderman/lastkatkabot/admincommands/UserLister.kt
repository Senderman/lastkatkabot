package com.senderman.lastkatkabot.admincommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.lastkatkabot.callbacks.Callbacks
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class UserLister {

    companion object {
        fun listUsers(handler: LastkatkaBotHandler, message: Message, type: DBService.UserType) {
            val users = Services.db.getTgUsersByType(type)
            val messageToSend = Methods.sendMessage().setChatId(message.chatId)
            var allAdminsAccess = false
            val title: String
            val callback: String
            when (type) {
                DBService.UserType.ADMINS -> {
                    title = "\uD83D\uDE0E <b>Админы бота:</b>\n"
                    callback = Callbacks.DELETE_ADMIN
                }
                DBService.UserType.BLACKLIST -> {
                    allAdminsAccess = true
                    title = "\uD83D\uDE3E <b>Список плохих кис:</b>\n"
                    callback = Callbacks.DELETE_NEKO
                }
                DBService.UserType.PREMIUM -> {
                    title = "\uD83D\uDC51 <b>Список премиум-пользователей:</b>\n"
                    callback = Callbacks.DELETE_PREM
                }

            }
            // show buttons only if user can change the list or if it is main admin's PM
            val showButtons = allAdminsAccess || message.chatId == Services.botConfig.mainAdmin.toLong()
            if (!showButtons || !message.isUserMessage) {
                val userlist = StringBuilder(title)
                val dropList = StringBuilder("\n")
                for (id in users) {
                    try {
                        val name = Methods.getChatMember(id.toLong(), id).call(handler).user.firstName
                        val user = TgUser(id, name)
                        userlist.append(user.link).append("\n")
                    } catch (e: Exception) {
                        Services.db.removeTGUser(id, type)
                        dropList.append("Юзер с id $id удален из бд!\n")
                    }
                }
                messageToSend.setText(userlist.append(dropList).toString())

            } else {
                val markup = InlineKeyboardMarkup()
                val rows = ArrayList<List<InlineKeyboardButton>>()
                var row: MutableList<InlineKeyboardButton> = ArrayList()
                for (id in users) {
                    val name = Methods.getChatMember(id.toLong(), id).call(handler).user.firstName
                    val user = TgUser(id, name)
                    row.add(InlineKeyboardButton().apply {
                        text = user.name
                        callbackData = callback + user.id
                    }
                    )
                    if (row.size == 2) {
                        rows.add(row)
                        row = ArrayList()
                    }
                }
                if (row.size == 1) {
                    rows.add(row)
                }
                rows.add(listOf(InlineKeyboardButton().apply {
                    text = "Закрыть меню"
                    callbackData = Callbacks.CLOSE_MENU
                }
                ))
                markup.keyboard = rows
                messageToSend.setText(title + "Для удаления пользователя нажмите на него").replyMarkup = markup
            }
            handler.sendMessage(messageToSend)
        }
    }
}


        
