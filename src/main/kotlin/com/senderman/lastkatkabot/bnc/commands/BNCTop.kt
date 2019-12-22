package com.senderman.lastkatkabot.bnc.commands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.neblib.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.lastkatkabot.bnc.BNCPlayer
import org.telegram.telegrambots.meta.api.objects.Message

class BNCTop (private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/top"
    override val desc: String
        get() = "топ игроков в Быки и Коровы"

    override fun execute(message: Message) {
        val chatId = message.chatId
        val top = Services.db.getTop()
        val text = StringBuilder("<b>Топ-10 задротов в bnc:</b>\n\n")
        var counter = 1
        for ((playerId, score) in top) {
            val member = Methods.getChatMember(playerId.toLong(), playerId).call(handler)
            val player = BNCPlayer(playerId, member.user.firstName, score)
            text.append(counter).append(": ")
            if (message.isUserMessage) text.append(player.link) else text.append(player.name)
            text.append(" (${player.score})\n")
            counter++
        }
        handler.sendMessage(chatId, text.toString())
    }
}