package com.senderman.lastkatkabot

import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.*
import com.senderman.lastkatkabot.DBService.UserType
import com.senderman.lastkatkabot.bnc.BullsAndCowsGame
import com.senderman.lastkatkabot.tempobjects.UserRow
import com.senderman.lastkatkabot.tempobjects.UserStats
import com.senderman.neblib.MongoClientKeeper
import org.bson.Document
import org.telegram.telegrambots.meta.api.objects.Message
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

internal class MongoDBService : DBService {
    private val timeZone = TimeZone.getTimeZone("Europe/Moscow")
    private val client = MongoClientKeeper.client
    private val lastkatkaDB: MongoDatabase = client.getDatabase("lastkatka")
    private val rouletteDB: MongoDatabase = client.getDatabase("roulette")
    private val rouletteUsers = rouletteDB["users"]
    private val admins = lastkatkaDB["admins"]
    private val premiumUsers = lastkatkaDB["premium"]
    private val blacklist = lastkatkaDB["blacklist"]
    private val userstats = lastkatkaDB["userstats"]
    private val settings = lastkatkaDB["settings"]
    private val bncgames = lastkatkaDB["bncgames"]
    private val chats = lastkatkaDB["chats"]

    private val set = "\$set"
    private val unset = "\$unset"
    private val pull = "\$pull"
    private val push = "\$push"

    private operator fun MongoDatabase.get(s: String): MongoCollection<Document> = getCollection(s)


    private fun MongoCollection<Document>.getUser(id: Int) = find(eq("id", id)).first() ?: initStats(id)

    private fun getUsersCollection(type: UserType): MongoCollection<Document> = when (type) {
        UserType.ADMINS -> admins
        UserType.BLACKLIST -> blacklist
        else -> premiumUsers
    }

    override fun initStats(id: Int): Document {
        val doc = Document("id", id)
            .append("total", 0)
            .append("wins", 0)
            .append("bnc", 0)
        userstats.insertOne(doc)
        return doc
    }

    override fun incTotalDuels(id: Int) {
        userstats.getUser(id)
        val updateDoc = Document().append("\$inc", Document("total", 1))
        userstats.updateOne(eq("id", id), updateDoc)
    }

    override fun incDuelWins(id: Int) {
        userstats.getUser(id)
        val updateDoc = Document().append("\$inc", Document("wins", 1).append("total", 1))
        userstats.updateOne(eq("id", id), updateDoc)
    }

    override fun incBNCWins(id: Int, points: Int) {
        val updateDoc = Document().append("\$inc", Document("bnc", points))
        userstats.updateOne(eq("id", id), updateDoc)
    }

    override fun getStats(id: Int): UserStats {
        val doc = userstats.getUser(id)
        val total = doc.getInteger("total")
        val wins = doc.getInteger("wins")
        val bncwins = doc.getInteger("bnc")
        val lover = doc.getInteger("lover") ?: 0
        val child = doc.getInteger("child") ?: 0
        val rouletteUser = rouletteUsers.find(eq("userId", id)).first()
        val coins = if (rouletteUser == null) 5000 else rouletteUser.getInteger("coins")
        return UserStats(id, wins, total, bncwins, lover, child, coins)
    }

    override fun getTop(): Map<Int, Int> {
        val bncPlayers = userstats
            .find(exists("bnc", true))
            .sort(Document("bnc", -1))
            .limit(10)
        val top = LinkedHashMap<Int, Int>()
        bncPlayers.forEach { top[it.getInteger("id")] = it.getInteger("bnc") }
        return top
    }

    override fun transferStats(fromId: Int, toId: Int) {
        val orig = userstats.getUser(fromId)
        val total = orig.getInteger("total")
        val wins = orig.getInteger("wins")
        val bnc = orig.getInteger("bnc")
        val commit = Document("total", total)
            .append("wins", wins)
            .append("bnc", bnc)
        userstats.getUser(toId)
        userstats.updateOne(
            eq("id", toId),
            Document("\$inc", commit)
        )
        userstats.deleteOne(eq("id", fromId))
    }

    override fun setUserCity(id: Int, city: String) {
        userstats.getUser(id)
        userstats.updateOne(eq("id", id), Document(set, Document("city", city)))
    }

    override fun getUserCity(id: Int): String? = userstats.getUser(id).getString("city")

    override fun setLover(userId: Int, loverId: Int) {
        with(userstats) {
            getUser(userId)
            getUser(loverId)
            updateOne(
                eq("id", userId), Document(
                    "\$set",
                    Document("lover", loverId)
                )
            )
            updateOne(
                eq("id", loverId), Document(
                    "\$set",
                    Document("lover", userId)
                )
            )
        }
    }

    override fun setChild(userId: Int, loverId: Int, childId: Int) {
        with(userstats) {
            getUser(userId)
            getUser(childId)
            updateMany(
                or(eq("id", userId), eq("id", loverId)), Document(
                    "\$set",
                    Document("child", childId)
                )
            )
            updateOne(
                eq("id", childId), Document(
                    "\$set",
                    Document("child", userId)
                )
            )
        }
    }

    override fun getLover(userId: Int): Int = userstats.getUser(userId).getInteger("lover") ?: 0

    override fun getChild(userId: Int): Int = userstats.getUser(userId).getInteger("child") ?: 0

    override fun divorce(userId: Int) {
        with(userstats) {
            val doc = getUser(userId)
            val lover = doc.getInteger("lover") ?: return
            updateMany(
                or(eq("id", lover), eq("id", userId)),
                Document("\$unset", Document("lover", 0))
            )
        }
    }

    override fun addTgUser(id: Int, type: UserType) {
        val collection = getUsersCollection(type)
        if (collection.find(eq("id", id)).first() == null)
            collection.insertOne(Document("id", id))
    }

    override fun removeTGUser(id: Int, type: UserType) {
        getUsersCollection(type).deleteOne(eq("id", id))
    }

    override fun getTgUsersByType(type: UserType): MutableSet<Int> {
        val result = HashSet<Int>()
        val collection = getUsersCollection(type)
        collection.find().forEach { result.add(it.getInteger("id")) }
        return result
    }

    override fun getAllUsersIds(): Set<Int> {
        val userIds = HashSet<Int>()
        for (chat in chats.find(exists("chatId", true))) {
            userIds += getChatMembersIds(chat.getLong("chatId"))
        }

        userstats.find().forEach { userIds += it.getInteger("id") }
        return userIds
    }


    override fun addUserToChat(message: Message) {
        val chatId = message.chatId
        val userId = message.from.id
        val date = message.date

        val filter = Document.parse("{chatId: $chatId, users: {\$elemMatch: {userId: $userId}}}")
        val doc = chats.find(filter).first()
        val chatHasUser: Boolean = doc != null
        if (chatHasUser) {
            chats.updateOne(
                filter,
                Document.parse("{$set: {users.\$.date: $date}}")
            )
        } else {
            chats.updateOne(
                eq("chatId", chatId),
                Document.parse("{$push: {users: {userId: $userId, date:$date} }}")
            )
        }
    }

    override fun removeUserFromChat(userId: Int, chatId: Long) {
        chats.updateOne(
            eq("chatId", chatId),
            Document.parse("{$pull: {users: {userId: $userId}}}")
        )
    }

    override fun getChatMembersIds(chatId: Long): MutableList<Int> {
        val members = ArrayList<Int>()
        val doc = chats.find(eq("chatId", chatId)).first() ?: return members
        members += doc.getList("users", Document::class.java).map { it.getInteger("userId") }
        return members
    }

    override fun removeOldUsers(chatId: Long, date: Int) {
        chats.updateOne(
            eq("chatId", chatId),
            Document.parse("{$pull: {users: {\$lt: {date: $date}}}}")
        )
    }

    override fun getBnCGames(): MutableMap<Long, BullsAndCowsGame> {
        val games = HashMap<Long, BullsAndCowsGame>()
        val gson = Gson()
        bncgames.find().forEach {
            val game = gson.fromJson(it.getString("game"), BullsAndCowsGame::class.java)
            games[it.getLong("chatId")] = game
        }
        return games
    }

    override fun saveBncGame(chatId: Long, game: BullsAndCowsGame) {
        val gameSaved = bncgames.find(eq("chatId", chatId)).first() != null
        val gson = Gson()
        val gameAsJson = gson.toJson(game)
        val commit = Document("game", gameAsJson)
        if (gameSaved) {
            bncgames.updateOne(
                eq("chatId", chatId),
                Document(set, commit)
            )
        } else {
            bncgames.insertOne(commit.append("chatId", chatId))
        }
    }

    override fun deleteBncGame(chatId: Long) {
        bncgames.deleteOne(eq("chatId", chatId))
    }

    override fun saveRow(chatId: Long, row: UserRow) {
        val gson = Gson()
        val rowAsJson = gson.toJson(row)
        chats.updateOne(
            eq("chatId", chatId),
            Document(set, Document("row", rowAsJson))
        )
    }

    override fun getUserRows(): MutableMap<Long, UserRow> {
        val rows = HashMap<Long, UserRow>()
        val gson = Gson()
        chats.find(exists("row", true)).forEach {
            val row = gson.fromJson(it.getString("row"), UserRow::class.java)
            rows[it.getLong("chatId")] = row
        }
        return rows
    }

    override fun deleteRow(chatId: Long) {
        chats.updateOne(
            eq("chatId", chatId),
            Document("\$unset", Document("row", ""))
        )
    }

    override fun getTournamentMessageId(): Int {
        val doc = settings.find(exists("messageId", true)).first() ?: return 0
        return doc.getInteger("messageId")
    }

    override fun setTournamentMessage(messageId: Int) {
        with(settings) {
            val doc = find(exists("messageId", true)).first()
            if (doc == null)
                insertOne(Document("messageId", messageId))
            else
                updateOne(
                    exists("messageId", true),
                    Document(set, Document("messageId", messageId))
                )
        }
    }

    override fun addChat(chatId: Long, title: String) {
        val doc = chats.find(eq("chatId", chatId)).first()
        if (doc == null)
            chats.insertOne(
                Document.parse("{chatId: $chatId, title: $title, users: []}")
            )
    }

    override fun getChatTitleMap(): Map<Long, String> {
        val result = HashMap<Long, String>()
        chats.find().forEach {
            result[it.getLong("chatId")] = it.getString("title") ?: "No title"
        }
        return result
    }

    override fun getChatIdsSet(): MutableSet<Long> {
        val allowedChats = HashSet<Long>()
        chats.find().forEach { allowedChats.add(it.getLong("chatId")) }
        return allowedChats
    }

    override fun updateChatId(oldChatId: Long, newChatId: Long) {
        chats.updateOne(
            eq("chatId", oldChatId),
            Document(set, Document("chatId", newChatId))
        )
    }

    override fun updateTitle(chatId: Long, title: String) {
        val commit = Document("title", title)
        chats.updateOne(eq("chatId", chatId), Document(set, commit))
    }

    override fun removeChat(chatId: Long) {
        chats.deleteOne(eq("chatId", chatId))
        bncgames.deleteOne(eq("chatId", chatId))
    }

    override fun cleanup() {
        userstats.deleteMany(
            and(
                eq("total", 0),
                eq("bnc", 0),
                exists("city", false)
            )
        )
    }

    override fun setPair(chatId: Long, pair: String) {
        var history = getPairsHistory(chatId)
        history = if (history == null) pair
        else pair + "\n" + history.lines().joinToString(separator = "\n", limit = 9, truncated = "")

        val date = Calendar.getInstance(timeZone).time
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        dateFormat.timeZone = timeZone
        val commit = Document("pair", pair).append("history", history)
        val hoursFormat = SimpleDateFormat("HH")
        hoursFormat.timeZone = timeZone
        var hours = hoursFormat.format(date).toInt()
        hours = if (hours in 0..11) 0 else 12
        commit.append("date", dateFormat.format(date).toLong())
            .append("hours", hours)
        if (chats.find(eq("chatId", chatId)).first() == null) {
            commit.append("chatId", chatId)
            chats.insertOne(commit)
        } else chats.updateOne(eq("chatId", chatId), Document(set, commit))
    }

    override fun pairExistsToday(chatId: Long): Boolean {
        val doc = chats.find(eq("chatId", chatId)).first() ?: return false
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        dateFormat.timeZone = timeZone
        val date = Calendar.getInstance(timeZone).time
        val today = dateFormat.format(date)
        if ("date" !in doc || doc.getLong("date") < today.toLong()) return false
        val hoursFormat = SimpleDateFormat("HH")
        hoursFormat.timeZone = timeZone
        var hours = hoursFormat.format(date).toInt()
        hours = if (hours in 0..11) 0 else 12
        return doc.getInteger("hours") == hours
    }

    override fun getPairOfTheDay(chatId: Long): String {
        val doc = chats.find(eq("chatId", chatId)).first()
        return doc!!.getString("pair")
    }

    override fun getPairsHistory(chatId: Long): String? {
        val doc = chats.find(eq("chatId", chatId)).first()
        return doc?.getString("history")
    }


}