package com.senderman.lastkatkabot.usercommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message

class ShortInfo constructor(val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/shortinfo"
    override val desc: String
        get() = "краткая инфа о сообщении. Поддерживается реплай"

    override fun execute(message: Message) {
        val chatId = message.chatId
        val userId = message.from.id
        var info = """
            ==== Информация ====
            
            💬 ID чата: $chatId
            🙍‍♂️ Ваш ID: $userId
        """.trimIndent()
        message.replyToMessage?.let { reply ->
            val replyMessageId = reply.messageId
            val replyUserId = reply.from.id
            info += """
                
                ✉️ ID reply: $replyMessageId
                🙍‍♂ ID юзера из reply: $replyUserId
            """.trimIndent()

            if (reply.chat.isChannelChat) {
                info += "\n\uD83D\uDCE2 ID канала: ${reply.chatId}"
            }
        }

        handler.sendMessage(chatId, info)
    }
}