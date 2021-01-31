package com.senderman.lastkatkabot

import com.senderman.lastkatkabot.bnc.BullsAndCowsGame
import com.senderman.lastkatkabot.tempobjects.UserRow
import com.senderman.lastkatkabot.tempobjects.UserStats
import org.bson.Document
import org.telegram.telegrambots.meta.api.objects.Message

interface DBService {

    enum class UserType {
        ADMINS, PREMIUM, BLACKLIST
    }

    fun initStats(id: Int): Document
    fun incTotalDuels(id: Int)
    fun incDuelWins(id: Int)
    fun incBNCWins(id: Int, points: Int)
    fun getStats(id: Int): UserStats

    // return map of <id, score> sorted by descending order
    fun getTop(): Map<Int, Int>

    fun transferStats(fromId: Int, toId: Int)

    fun setUserCity(id: Int, city: String)
    fun getUserCity(id: Int): String?
    fun setLover(userId: Int, loverId: Int)
    fun setChild(userId: Int, loverId: Int, childId: Int)
    fun getLover(userId: Int): Int
    fun getChild(userId: Int): Int
    // TOOD implement fun hasParent(userId: Int): Boolean
    fun divorce(userId: Int)


    fun addTgUser(id: Int, type: UserType)
    fun removeTGUser(id: Int, type: UserType)
    fun getTgUsersByType(type: UserType): MutableSet<Int>


    fun getAllUsersIds(): Set<Int>
    fun addUserToChat(chatId: Long, userId:Int, date:Int)
    fun removeUserFromChat(userId: Int, chatId: Long)
    fun getChatMembersIds(chatId: Long): MutableList<Int>
    fun removeOldUsers(chatId: Long, date: Int)


    fun getBnCGames(): MutableMap<Long, BullsAndCowsGame>
    fun saveBncGame(chatId: Long, game: BullsAndCowsGame)
    fun deleteBncGame(chatId: Long)

    fun saveRow(chatId: Long, row: UserRow)
    fun getUserRows(): MutableMap<Long, UserRow>
    fun deleteRow(chatId: Long)

    fun getTournamentMessageId(): Int
    fun setTournamentMessage(messageId: Int)


    fun addChat(chatId: Long, title: String)
    fun getChatTitleMap(): Map<Long, String>
    fun getChatIdsSet(): MutableSet<Long>
    fun updateChatId(oldChatId: Long, newChatId: Long)
    fun updateTitle(chatId: Long, title: String)
    fun removeChat(chatId: Long)
    fun cleanup()


    fun setPair(chatId: Long, pair: String)
    fun pairExistsToday(chatId: Long): Boolean
    fun getPairOfTheDay(chatId: Long): String
    fun getPairsHistory(chatId: Long): String?
}