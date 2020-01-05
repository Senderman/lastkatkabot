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
    private val rouletteUsers = rouletteDB.getCollection("users")
    private val chatMembersDB = client.getDatabase("chatmembers")
    private val admins = lastkatkaDB.getCollection("admins")
    private val premiumUsers = lastkatkaDB.getCollection("premium")
    private val blacklist = lastkatkaDB.getCollection("blacklist")
    private val userstats = lastkatkaDB.getCollection("userstats")
    private val settings = lastkatkaDB.getCollection("settings")
    private val bncgames = lastkatkaDB.getCollection("bncgames")
    private val chats = lastkatkaDB.getCollection("chats")


    private fun MongoCollection<Document>.getUser(id: Int): Document {
        return find(eq("id", id)).first() ?: initStats(id)
    }

    private fun getChatMembersCollection(chatId: Long): MongoCollection<Document> {
        return chatMembersDB.getCollection(chatId.toString())
    }

    private fun getUsersCollection(type: UserType): MongoCollection<Document> {
        return when (type) {
            UserType.ADMINS -> admins
            UserType.BLACKLIST -> blacklist
            else -> premiumUsers
        }
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
        val rouletteUser = rouletteUsers.find(eq("userId", id)).first()
        val coins = if (rouletteUser == null) 5000 else rouletteUser.getInteger("coins")
        return UserStats(id, wins, total, bncwins, lover, coins)
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
        userstats.updateOne(eq("id", id), Document("\$set", Document("city", city)))
    }

    override fun getUserCity(id: Int): String? {
        return userstats.getUser(id).getString("city")
    }

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

    override fun getLover(userId: Int): Int = userstats.getUser(userId).getInteger("lover") ?: 0

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
        chatMembersDB.listCollectionNames().forEach { name ->
            chatMembersDB.getCollection(name).find().forEach {
                userIds.add(it.getInteger("id"))
            }
        }
        userstats.find().forEach { userIds.add(it.getInteger("id")) }
        return userIds
    }


    override fun addUserToChatDB(message: Message) {
        val chatId = message.chatId
        val user = message.from
        val chat = getChatMembersCollection(chatId)
        val commit = Document("lastMessageDate", message.date)
        val doc = chat.find(eq("id", user.id)).first()
        if (doc == null) {
            commit.append("id", user.id)
            chat.insertOne(commit)
        } else chat.updateOne(eq("id", user.id), Document("\$set", commit))
    }

    override fun removeUserFromChatDB(userId: Int, chatId: Long) {
        getChatMembersCollection(chatId).deleteOne(eq("id", userId))
    }

    override fun getChatMemebersIds(chatId: Long): MutableList<Int> {
        val chat = getChatMembersCollection(chatId)
        val members = ArrayList<Int>()
        chat.find().forEach { members.add(it.getInteger("id")) }
        return members
    }

    override fun removeOldUsers(chatId: Long, date: Int) {
        getChatMembersCollection(chatId).deleteMany(lt("lastMessageDate", date))
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
                Document("\$set", commit)
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
            Document("\$set", Document("row", rowAsJson))
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
                    Document("\$set", Document("messageId", messageId))
                )
        }
    }

    override fun addChat(chatId: Long, title: String) {
        chats.insertOne(Document("chatId", chatId).append("title", title))
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
            Document("\$set", Document("chatId", newChatId))
        )
    }

    override fun updateTitle(chatId: Long, title: String) {
        val commit = Document("title", title)
        chats.updateOne(eq("chatId", chatId), Document("\$set", commit))
    }

    override fun removeChat(chatId: Long) {
        chats.deleteOne(eq("chatId", chatId))
        getChatMembersCollection(chatId).drop()
        bncgames.deleteOne(eq("chatId", chatId))
    }

    override fun cleanup() {
        chatMembersDB.listCollectionNames().forEach { name ->
            if (
                chats.find(eq("chatId", name.toLong())).first() == null
                || getChatMembersCollection(name.toLong()).countDocuments() < 2
            ) {
                getChatMembersCollection(name.toLong()).drop()
                chats.deleteOne(eq("chatId", name.toLong()))
            }
        }
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
        } else chats.updateOne(eq("chatId", chatId), Document("\$set", commit))
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