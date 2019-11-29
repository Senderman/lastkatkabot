package com.senderman.lastkatkabot

import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.*
import com.senderman.MongoClientKeeper
import com.senderman.lastkatkabot.DBService.UserType
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame
import com.senderman.lastkatkabot.tempobjects.UserRow
import org.bson.Document
import org.telegram.telegrambots.meta.api.objects.Message
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class MongoDBService : DBService {
    private val timeZone = TimeZone.getTimeZone("Europe/Moscow")
    private val client = MongoClientKeeper.client
    private val lastkatkaDB = client.getDatabase("lastkatka")
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

    override fun getStats(id: Int): Map<String, Int> {
        val doc = userstats.getUser(id)
        val total = doc.getInteger("total")
        val wins = doc.getInteger("wins")
        val bncwins = doc.getInteger("bnc")
        val lover = doc.getInteger("lover") ?: 0
        val stats = HashMap<String, Int>()
        stats["total"] = total
        stats["wins"] = wins
        stats["bnc"] = bncwins
        stats["lover"] = lover
        return stats
    }

    override fun getTop(): Map<Int, Int> {
        val bnsPlayers = userstats
                .find(exists("bnc", true))
                .sort(Document("bnc", -1))
                .limit(10)
        val top = LinkedHashMap<Int, Int>()
        for (doc in bnsPlayers) {
            top[doc.getInteger("id")] = doc.getInteger("bnc")
        }
        return top
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
            updateOne(eq("id", userId), Document("\$set",
                    Document("lover", loverId)))
            updateOne(eq("id", loverId), Document("\$set",
                    Document("lover", userId)))
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
        for (doc in collection.find()) {
            result.add(doc.getInteger("id"))
        }
        return result
    }

    override fun getAllUsersIds(): Set<Int> {
        val userIds = HashSet<Int>()
        for (collName in chatMembersDB.listCollectionNames()) {
            for (doc in chatMembersDB.getCollection(collName).find()) {
                userIds.add(doc.getInteger("id"))
            }
        }
        for (doc in userstats.find()) {
            userIds.add(doc.getInteger("id"))
        }
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

    override fun getChatMemebersIds(chatId: Long): List<Int> {
        val chat = getChatMembersCollection(chatId)
        val members = ArrayList<Int>()
        for (doc in chat.find()) {
            members.add(doc.getInteger("id"))
        }
        return members
    }

    override fun removeOldUsers(chatId: Long, date: Int) {
        getChatMembersCollection(chatId).deleteMany(lt("lastMessageDate", date))
    }

    override fun getBnCGames(): MutableMap<Long, BullsAndCowsGame> {
        val games = HashMap<Long, BullsAndCowsGame>()
        val gson = Gson()
        for (doc in bncgames.find()) {
            val game = gson.fromJson(doc.getString("game"), BullsAndCowsGame::class.java)
            games[doc.getLong("chatId")] = game
        }
        return games
    }

    override fun saveBncGame(chatId: Long, game: BullsAndCowsGame) {
        val gameSaved = bncgames.find(eq("chatId", chatId)).first() != null
        val gson = Gson()
        val gameAsJson = gson.toJson(game)
        val commit = Document("game", gameAsJson)
        if (gameSaved) {
            bncgames.updateOne(eq("chatId", chatId),
                    Document("\$set", commit))
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
        chats.updateOne(eq("chatId", chatId),
                Document("\$set", Document("row", rowAsJson)))
    }

    override fun getUserRows(): MutableMap<Long, UserRow> {
        val rows = HashMap<Long, UserRow>()
        val gson = Gson()
        for (doc in chats.find(exists("row", true))) {
            val row = gson.fromJson(doc.getString("row"), UserRow::class.java)
            rows[doc.getLong("chatId")] = row
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
                updateOne(exists("messageId", true),
                        Document("\$set", Document("messageId", messageId)))
        }
    }

    override fun addAllowedChat(chatId: Long, title: String) {
        chats.insertOne(Document("chatId", chatId).append("title", title))
    }

    override fun getAllowedChatsMap(): Map<Long, String> {
        val chats = HashMap<Long, String>()
        for (doc in this.chats.find()) {
            chats[doc.getLong("chatId")] = doc.getString("title")
        }
        return chats
    }

    override fun getAllowedChatsSet(): MutableSet<Long> {
        val allowedChats = HashSet<Long>()
        for (doc in chats.find()) {
            allowedChats.add(doc.getLong("chatId"))
        }
        return allowedChats
    }

    override fun updateChatId(oldChatId: Long, newChatId: Long) {
        chats.updateOne(eq("chatId", oldChatId),
                Document("\$set", Document("chatId", newChatId)))
    }

    override fun updateTitle(chatId: Long, title: String) {
        val commit = Document("title", title)
        chats.updateOne(eq("chatId", chatId), Document("\$set", commit))
    }

    override fun removeAllowedChat(chatId: Long) {
        chats.deleteOne(eq("chatId", chatId))
        getChatMembersCollection(chatId).drop()
    }

    override fun cleanup() {
        for (chat in chatMembersDB.listCollectionNames()) {
            if (chats.find(eq("chatId", chat.toLong())).first() == null)
                getChatMembersCollection(chat.toLong()).drop()
        }
    }

    override fun setPair(chatId: Long, pair: String) {
        var history = getPairsHistory(chatId)
        history = if (history == null) pair
        else pair + "\n" +
                java.lang.String(history)
                        .lines()
                        .limit(9)
                        .collect(Collectors.joining("\n"))

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
        if (!doc.containsKey("date") || doc.getLong("date") < today.toLong()) return false
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