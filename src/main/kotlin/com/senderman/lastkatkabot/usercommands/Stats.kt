package com.senderman.lastkatkabot.usercommands

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class Stats(private val handler: LastkatkaBotHandler) : CommandExecutor {
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
        val (_, duelWins, totalDuels, bnc, loverId, childId, coins) = stats
        val winRate = if (totalDuels == 0) 0 else 100 * duelWins / totalDuels
        var text = """
            📊 Статистика ${user.name}:

            Дуэлей выиграно: $duelWins
            Всего дуэлей: $totalDuels
            Винрейт: $winRate%
            
            💰 Деньги: $coins
            🐮 Баллов за быки и коровы: $bnc
        """.trimIndent()

        text += formatStats(message, loverId, StatsFormat.LOVER)
        text += formatStats(message, childId, StatsFormat.CHILD)

        handler.sendMessage(message.chatId, text)
    }

    enum class StatsFormat {
        LOVER, CHILD
    }

    private fun formatStats(message: Message, userId: Int, type: StatsFormat): String {
        var text = ""
        val user =
            try {
                TgUser(handler.execute(GetChatMember().setChatId(userId.toLong()).setUserId(userId)).user)
            } catch (e: TelegramApiException) {
                try {
                    TgUser(handler.execute(GetChatMember().setChatId(message.chatId).setUserId(userId)).user)
                } catch (e: TelegramApiException) {
                    TgUser(userId, "Без имени")
                }
            }
        text += when (type) {
            StatsFormat.CHILD -> {
                "\n\uD83D\uDC76\uD83C\uDFFB️ Ребенок: "
            }
            StatsFormat.LOVER -> {
                "\n❤️ Вторая половинка: "
            }
        }
        text += if (message.isUserMessage) user.link else user.name
        return text
    }
}