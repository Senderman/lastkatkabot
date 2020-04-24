package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.Message

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

        text += connectUserToStats(message, loverId, AnotherUser.LOVER)
        text += connectUserToStats(message, childId, AnotherUser.CHILD)
        text += "\n\nХочешь больше монеток? Играй в @miniroulette_bot!"
        handler.sendMessage(message.chatId, text)
    }

    private enum class AnotherUser(val title: String) {
        LOVER("\n❤️ Вторая половинка: "),
        CHILD("\n\uD83D\uDC76\uD83C\uDFFB️ Ребенок: ")
    }

    /**
     * Add line about user connected to userId
     * @param message - message object of /stats caller
     * @param userId - ID of connected user
     * @param type - type of connected user
     * @return empty string if userId == 0, else type.title + link to user if message is pm, or user'name if not pm
     */

    private fun connectUserToStats(message: Message, userId: Int, type: AnotherUser): String {
        if (userId == 0)
            return ""

        val user: TgUser =
            try {
                TgUser(Methods.getChatMember(userId.toLong(), userId).call(handler).user)
            } catch (e: Exception) {
                try {
                    TgUser(Methods.getChatMember(message.chatId, userId).call(handler).user)
                } catch (e: Exception) {
                    TgUser(userId, "Без имени")
                }
            }

        return type.title + if (message.isUserMessage) user.link else user.name
    }
}