package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.TgUser
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.logging.BotLogger

class Pair (private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/pair"
    override val desc: String
        get() = "пара дня"

    private data class Lover(val user: TgUser, val isTrueLover: Boolean)

    override fun execute(message: Message) {
        if (message.isUserMessage) return

        val chatId = message.chatId
        // check for existing pair
        if (Services.db.pairExistsToday(chatId)) {
            var pair = Services.db.getPairOfTheDay(chatId)
            pair = "Пара дня: $pair"
            handler.sendMessage(chatId, pair)
            return
        }

        // remove users without activity for 2 weeks and get list of actual users
        Services.db.removeOldUsers(chatId, message.date - 1209600)
        val userIds = Services.db.getChatMemebersIds(chatId)

        // generate 2 different random users
        val user1: TgUser
        val user2: TgUser
        val isTrueLove: Boolean
        try {
            user1 = getUserForPair(chatId, userIds)
            userIds.remove(user1.id)
            val lover = getSecondUserForPair(chatId, userIds, user1)
            user2 = lover.user
            isTrueLove = lover.isTrueLover
        } catch (e: Exception) {
            handler.sendMessage(
                chatId,
                "Недостаточно пользователей для создания пары! Подождите, пока кто-то еще напишет в чат!"
            )
            return
        }
        // get a random text and set up a pair
        val loveArray = Services.botConfig.loveStrings
        val loveStrings = loveArray.random().trim().split("\n")
        try {
            for (i in 0 until loveStrings.lastIndex) {
                handler.sendMessage(chatId, loveStrings[i])
                Thread.sleep(1500)
            }
        } catch (e: InterruptedException) {
            BotLogger.error("PAIR", "Ошибка таймера")
        }
        val pair = if (isTrueLove) "${user1.name} \uD83D\uDC96 ${user2.name}" else "${user1.name} ❤ ${user2.name}"
        Services.db.setPair(chatId, pair)
        handler.sendMessage(chatId, java.lang.String.format(loveStrings.last(), user1.link, user2.link))
    }

    @Throws(Exception::class)
    private fun getSecondUserForPair(chatId: Long, userIds: MutableList<Int>, first: TgUser): Lover {
        val loverId = Services.db.getLover(first.id)
        return if (loverId in userIds) {
            Lover(TgUser(Methods.getChatMember(chatId, loverId).call(handler).user), true)
        } else Lover(getUserForPair(chatId, userIds), false)
    }

    @Throws(Exception::class)
    private fun getUserForPair(chatId: Long, userIds: MutableList<Int>): TgUser {
        while (userIds.size > 2) {
            val userId = userIds.random()
            Methods.getChatMember(chatId, userId).call(handler)?.let { member ->
                return TgUser(member.user)
            }
            Services.db.removeUserFromChatDB(userId, chatId)
            userIds.remove(userId)
        }
        throw Exception("Not enough users")
    }
}