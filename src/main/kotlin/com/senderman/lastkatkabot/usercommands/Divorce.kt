package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message

class Divorce(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/divorce"
    override val desc: String
        get() = "подать на развод"

    override fun execute(message: Message) {
        val chatId = message.chatId
        val userId = message.from.id
        val loverId = Services.db.getLover(userId)
        if (loverId == 0) {
            handler.sendMessage(chatId, "У вас и так никого нет!")
            return
        }
        Services.db.divorce(userId)
        handler.sendMessage(chatId, "Вы расстались со своей половинкой! А ведь так все хорошо начиналось...")
        val user = TgUser(Methods.getChatMember(userId.toLong(), userId).call(handler).user)
        try {
            handler.execute(
                SendMessage(
                    loverId.toLong(),
                    "Ваша половинка (${user.link}) покинула вас... Теперь вы одни..."
                ).enableHtml(true)
            )
        } catch (ignored: Exception) {
        }
    }
}