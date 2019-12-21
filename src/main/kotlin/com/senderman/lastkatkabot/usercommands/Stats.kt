package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.TgUser
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message

class Stats (private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/stats"
    override val desc: String
        get() = "статистика. Реплаем можно узнать статистику реплайнутого"

    override fun execute(message: Message) {
        val player = if (!message.isReply) message.from else message.replyToMessage.from
        if (player.bot) {
            handler.sendMessage(
                message.chatId, "Но это же просто бот, имитация человека! " +
                        "Разве может бот написать симфонию, иметь статистику, играть в BnC, любить?"
            )
            return
        }
        val user = TgUser(player)
        val stats = Services.db.getStats(player.id)
        val (_, duelWins, totalDuels, bnc, loverId) = stats
        val winRate = if (totalDuels == 0) 0 else 100 * duelWins / totalDuels
        var text = """
            📊 Статистика ${user.name}:

            Дуэлей выиграно: $duelWins
            Всего дуэлей: $totalDuels
            Винрейт: $winRate%
            
            🐮 Баллов за быки и коровы: $bnc
        """.trimIndent()
        if (loverId != 0) {
            val lover = TgUser(Methods.getChatMember(loverId.toLong(), loverId).call(handler).user)
            text += "\n❤️ Вторая половинка: "
            text += if (message.isUserMessage) lover.link else lover.name
        }
        handler.sendMessage(message.chatId, text)
    }
}