package com.senderman.lastkatkabot

import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.senderman.lastkatkabot.DBService.UserType
import com.senderman.lastkatkabot.bnc.BullsAndCowsGame
import com.senderman.lastkatkabot.tempobjects.UserRow
import com.senderman.lastkatkabot.tempobjects.UserStats
import com.senderman.neblib.MongoClientKeeper
import org.bson.Document
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
    private val pull = "\$pull"
    private val push = "\$push"
    private val elemMatch = "\$elemMatch"

    private operator fun MongoDatabase.get(s: String): MongoCollection<Document> = getCollection(s)


    private fun MongoCollection<Document>.getUser(id: Int) = find(eq("id", id)).first() ?: initStats(id)

    private fun getUsersCollection(type: UserType): MongoCollection<Document> = when (type) {
        UserType.ADMINS -> admins
        UserType.BLACKLIST -> blacklist
        else -> premiumUsers
    }

    override fun initStats(id: Int): Document {
        val doc = Document()
        doc["id"] = id
        doc["total"] = 0
        doc["wins"] = 0
        doc["bnc"] = 0
        userstats.insertOne(doc)
        return doc
    }

    override fun incTotalDuels(id: Int) {
        userstats.getUser(id)
        userstats.updateOne(
            eq("id", id),
            Updates.inc("total", 1)
        )
    }

    override fun incDuelWins(id: Int) {
        userstats.getUser(id)
        val updateDoc = Document().append("\$inc", Document("wins", 1).append("total", 1))
        userstats.updateOne(eq("id", id), updateDoc)
    }

    override fun incBNCWins(id: Int, points: Int) {
        userstats.updateOne(
            eq("id", id),
            Updates.inc("bnc", points)
        )
    }

    override fun getStats(id: Int): UserStats {
        val doc = userstats.getUser(id)
        val total = doc.getInteger("total")
        val wins = doc.getInteger("wins")
        val bncwins = doc.getInteger("bnc")
        val lover = doc.getInteger("lover") ?: 0
        val child = doc.getInteger("child") ?: 0
        val rouletteUser = rouletteUsers.find(eq("userId", id)).first()
        val coins = rouletteUser?.getInteger("coins") ?: 5000
        return UserStats(id, wins, total, bncwins, lover, child, coins)
    }

    override fun getTop(): Map<Int, Int> {
        val bncPlayers = userstats
            .find(exists("bnc", true))
            .sort(Document("bnc", -1))
            .limit(10)
        val top = LinkedHashMap<Int, Int>()
        for (doc in bncPlayers)
            top[doc.getInteger("id")] = doc.getInteger("bnc")
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
        userstats.updateOne(
            eq("id", id),
            Updates.set("city", city)
        )
    }

    override fun getUserCity(id: Int): String? = userstats.getUser(id).getString("city")

    override fun setLover(userId: Int, loverId: Int) {
        with(userstats) {
            getUser(userId)
            getUser(loverId)
            updateOne(
                eq("id", userId),
                Updates.set("lover", loverId)
            )
            updateOne(
                eq("id", loverId),
                Updates.set("lover", userId)
            )
        }
    }

    override fun setChild(userId: Int, loverId: Int, childId: Int) {
        with(userstats) {
            getUser(userId)
            getUser(childId)
            updateMany(
                or(eq("id", userId), eq("id", loverId)),
                Updates.set("child", childId)
            )
            updateOne(
                eq("id", childId),
                Updates.set("child", userId)
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
                Updates.unset("lover")
            )
        }
    }

    override fun addTgUser(id: Int, type: UserType) {
        val collection = getUsersCollection(type)
        collection.find(eq("id", id)).first() ?: collection.insertOne(Document("id", id))
    }

    override fun removeTGUser(id: Int, type: UserType) {
        getUsersCollection(type).deleteOne(eq("id", id))
    }

    override fun getTgUsersByType(type: UserType): MutableSet<Int> = getUsersCollection(type)
        .find()
        .map { it.getInteger("id") }
        .toMutableSet()


    override fun getAllUsersIds(): Set<Int> {
        val userIds = HashSet<Int>()
        for (chat in chats.find()) {
            userIds += getChatMembersIds(chat.getLong("chatId"))
        }

        userIds += userstats.find().map { it.getInteger("id") }
        return userIds
    }


    override fun addUserToChat(chatId: Long, userId: Int, date: Int) {

        val filter = Document.parse("{chatId: $chatId, users: {$elemMatch: {userId: $userId}}}")
        val doc = chats.find(filter).first()
        val chatHasUser: Boolean = doc != null
        if (chatHasUser) {
            chats.updateOne(
                filter,
                Document.parse("{$set: {'users.$.date': $date}}")
            )
        } else {
            chats.updateOne(
                eq("chatId", chatId),
                Document.parse("{$push: {users: {userId: $userId, date: $date} }}"),
                UpdateOptions().upsert(true)
            )
        }
    }

    override fun removeUserFromChat(userId: Int, chatId: Long) {
        chats.updateOne(
            eq("chatId", chatId),
            Document.parse("{$pull: {users: {userId: $userId}}}")
        )
    }

    override fun getChatMembersIds(chatId: Long): MutableList<Int> =
        chats.find(eq("chatId", chatId))
            .flatMap { it.getList("users", Document::class.java) }
            .map { it.getInteger("userId") }
            .toMutableList()

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
        val gson = Gson()
        val gameAsJson = gson.toJson(game)
        val commit = Document("game", gameAsJson).append("chatId", chatId)
        bncgames.updateOne(
            eq("chatId", chatId),
            Document(set, commit),
            UpdateOptions().upsert(true)
        )
    }

    override fun deleteBncGame(chatId: Long) {
        bncgames.deleteOne(eq("chatId", chatId))
    }

    override fun saveRow(chatId: Long, row: UserRow) {
        val gson = Gson()
        val rowAsJson = gson.toJson(row)
        chats.updateOne(
            eq("chatId", chatId),
            Document(set, Document("row", rowAsJson)),
            UpdateOptions().upsert(true)
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
        settings.updateOne(
            exists("messageId", true),
            Updates.set("messageId", messageId),
            UpdateOptions().upsert(true)
        )
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

    override fun getChatIdsSet(): MutableSet<Long> = chats
        .find()
        .map { it.getLong("chatId") }
        .toMutableSet()

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