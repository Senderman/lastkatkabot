package com.senderman.lastkatkabot;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.senderman.MongoClientKeeper;
import com.senderman.lastkatkabot.tempobjects.BnCPlayer;
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame;
import com.senderman.lastkatkabot.tempobjects.UserRow;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

public class MongoDBService implements DBService {
    private final TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
    private final MongoClient client = MongoClientKeeper.getClient();
    private final MongoDatabase lastkatkaDB = client.getDatabase("lastkatka");
    private final MongoDatabase chatMembersDB = client.getDatabase("chatmembers");
    private final MongoCollection<Document> admins = lastkatkaDB.getCollection("admins");
    private final MongoCollection<Document> premiumUsers = lastkatkaDB.getCollection("premium");
    private final MongoCollection<Document> blacklist = lastkatkaDB.getCollection("blacklist");
    private final MongoCollection<Document> userstats = lastkatkaDB.getCollection("userstats");
    private final MongoCollection<Document> settings = lastkatkaDB.getCollection("settings");
    private final MongoCollection<Document> bncgames = lastkatkaDB.getCollection("bncgames");
    private final MongoCollection<Document> chats = lastkatkaDB.getCollection("chats");

    private MongoCollection<Document> getChatMembersCollection(long chatId) {
        return chatMembersDB.getCollection(String.valueOf(chatId));
    }

    private MongoCollection<Document> getUsersCollection(COLLECTION_TYPE type) {
        if (type == COLLECTION_TYPE.ADMINS)
            return admins;
        else if (type == COLLECTION_TYPE.BLACKLIST)
            return blacklist;
        else
            return premiumUsers;
    }

    public void initStats(int id) {
        var doc = new Document("id", id)
                .append("total", 0)
                .append("wins", 0)
                .append("bnc", 0);
        userstats.insertOne(doc);
    }

    public void incDuelWins(int id) {
        var doc = userstats.find(eq("id", id)).first();
        if (doc == null)
            initStats(id);

        var updateDoc = new Document()
                .append("$inc", new Document("wins", 1).append("total", 1));

        userstats.updateOne(eq("id", id), updateDoc);
    }

    public void incTotalDuels(int id) {
        var doc = userstats.find(eq("id", id)).first();
        if (doc == null)
            initStats(id);

        var updateDoc = new Document()
                .append("$inc", new Document("total", 1));
        userstats.updateOne(eq("id", id), updateDoc);
    }

    @Override
    public void incBNCWins(int id, int points) {
        var doc = userstats.find(eq("id", id)).first();
        if (doc == null)
            initStats(id);

        var updateDoc = new Document()
                .append("$inc", new Document("bnc", points));
        userstats.updateOne(eq("id", id), updateDoc);
    }

    public Map<String, Integer> getStats(int id) {
        int total = 0, wins = 0, bncwins = 0, lover = 0;
        var doc = userstats.find(eq("id", id)).first();
        if (doc == null) {
            initStats(id);
        } else {
            total = doc.getInteger("total");
            wins = doc.getInteger("wins");
            bncwins = doc.getInteger("bnc");
            lover = doc.getInteger("lover");
        }
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("wins", wins);
        stats.put("bnc", bncwins);
        stats.put("lover", lover);
        return stats;
    }

    @Override
    public List<BnCPlayer> getTop() {
        var bnsPlayers = userstats
                .find(exists("bnc", true))
                .sort(new Document("bnc", -1))
                .limit(10);
        List<BnCPlayer> top = new ArrayList<>(10);
        for (var doc : bnsPlayers) {
            top.add(new BnCPlayer(doc.getInteger("id"), "Без имени", doc.getInteger("bnc")));
        }
        return top;
    }

    @Override
    public void setUserCity(int id, String city) {
        var doc = userstats.find(eq("id", id)).first();
        if (doc == null)
            initStats(id);

        userstats.updateOne(eq("id", id), new Document("$set", new Document("city", city)));
    }

    @Override
    public String getUserCity(int id) {
        var doc = userstats.find(eq("id", id)).first();
        if (doc == null)
            return null;

        return doc.getString("city");
    }

    @Override
    public void setLover(int userId, int loverId) {
        var doc = userstats.find(eq("id", userId)).first();
        if (doc == null)
            initStats(userId);

        var lover = userstats.find(eq("id", loverId)).first();
        if (lover == null)
            initStats(loverId);

        userstats.updateOne(eq("id", userId), new Document("$set",
                new Document("lover", loverId)));
        userstats.updateOne(eq("id", lover), new Document("$set",
                new Document("lover", userId)));
    }

    @Override
    public int getLover(int userId) {
        var doc = userstats.find(eq("id", userId)).first();
        if (doc == null || doc.getInteger("lover") == null)
            return 0;
        return doc.getInteger("lover");
    }

    @Override
    public void divorce(int userId) {
        var doc = userstats.find(eq("id", userId)).first();
        if (doc == null)
            return;
        if (doc.getInteger("lover") == null)
            return;

        int lover = doc.getInteger("lover");

        userstats.updateMany(
                or(
                        eq("id", lover),
                        eq("id", userId)
                ),
                new Document("$unset", new Document("lover", 0)));

    }

    @Override
    public void addTgUser(int id, COLLECTION_TYPE type) {
        var collection = getUsersCollection(type);
        if (collection.find(eq("id", id)).first() == null)
            collection.insertOne(new Document("id", id));

    }

    @Override
    public void removeTGUser(int id, COLLECTION_TYPE type) {
        getUsersCollection(type).deleteOne(eq("id", id));
    }

    @Override
    public Set<Integer> getTgUsersFromList(COLLECTION_TYPE collection_type) {
        Set<Integer> result = new HashSet<>();
        var collection = getUsersCollection(collection_type);
        for (var doc : collection.find()) {
            result.add(doc.getInteger("id"));
        }
        return result;
    }

    @Override
    public Set<Integer> getTgUsersIds(COLLECTION_TYPE collection_type) {
        Set<Integer> result = new HashSet<>();
        var collection = getUsersCollection(collection_type);
        for (var doc : collection.find()) {
            result.add(doc.getInteger("id"));
        }
        return result;
    }

    public Set<Integer> getAllUsersIds() {
        Set<Integer> userIds = new HashSet<>();
        for (String collName : chatMembersDB.listCollectionNames()) {
            for (var doc : chatMembersDB.getCollection(collName).find()) {
                userIds.add(doc.getInteger("id"));
            }
        }
        for (var doc : userstats.find()) {
            userIds.add(doc.getInteger("id"));
        }
        return userIds;
    }

    @Override
    public void addUserToChatDB(Message message) {
        var chatId = message.getChatId();
        var user = message.getFrom();
        var chat = getChatMembersCollection(chatId);
        var commit = new Document("lastMessageDate", message.getDate());

        var doc = chat.find(eq("id", user.getId())).first();
        if (doc == null) {
            commit.append("id", user.getId());
            chat.insertOne(commit);
        } else
            chat.updateOne(eq("id", user.getId()), new Document("$set", commit));
    }

    @Override
    public void removeUserFromChatDB(int userId, long chatId) {
        getChatMembersCollection(chatId).deleteOne(eq("id", userId));
    }

    @Override
    public List<Integer> getChatMemebersIds(long chatId) {
        var chat = getChatMembersCollection(chatId);
        List<Integer> members = new ArrayList<>();
        for (var doc : chat.find()) {
            members.add(doc.getInteger("id"));
        }
        return members;
    }

    @Override
    public void removeOldUsers(long chatId, int date) {
        var chat = getChatMembersCollection(chatId);
        chat.deleteMany(lt("lastMessageDate", date));
    }

    @Override
    public Map<Long, BullsAndCowsGame> getBnCGames() {
        Map<Long, BullsAndCowsGame> games = new HashMap<>();
        var gson = new Gson();
        for (var doc : bncgames.find()) {
            var game = gson.fromJson(doc.getString("game"), BullsAndCowsGame.class);
            games.put(doc.getLong("chatId"), game);
        }
        return games;
    }

    @Override
    public void saveBncGame(long chatId, BullsAndCowsGame game) {
        var gameSaved = bncgames.find(eq("chatId", chatId)).first() != null;
        var gson = new Gson();
        var gameAsJson = gson.toJson(game);
        var commit = new Document("game", gameAsJson);
        if (gameSaved) {
            bncgames.updateOne(eq("chatId", chatId),
                    new Document("$set", commit));
        } else {
            bncgames.insertOne(commit.append("chatId", chatId));
        }
    }

    @Override
    public void deleteBncGame(long chatId) {
        bncgames.deleteOne(eq("chatId", chatId));
    }

    @Override
    public void saveRow(long chatId, UserRow row) {
        var gson = new Gson();
        var rowAsJson = gson.toJson(row);
        chats.updateOne(eq("chatId", chatId),
                new Document("$set", new Document("row", rowAsJson)));
    }

    @Override
    public Map<Long, UserRow> getUserRows() {
        Map<Long, UserRow> rows = new HashMap<>();
        var gson = new Gson();
        for (var doc : chats.find()) {
            var row = gson.fromJson(doc.getString("row"), UserRow.class);
            rows.put(doc.getLong("chatId"), row);
        }
        return rows;
    }

    @Override
    public int getTournamentMessageId() {
        var doc = settings.find(exists("messageId", true)).first();
        if (doc == null)
            return 0;
        return doc.getInteger("messageId");
    }

    @Override
    public void setTournamentMessage(int messageId) {
        var doc = settings.find(exists("messageId", true)).first();
        if (doc == null)
            settings.insertOne(new Document("messageId", messageId));
        else
            settings.updateOne(exists("messageId", true),
                    new Document(
                            "$set", new Document("messageId", messageId)
                    ));
    }

    @Override
    public Set<Long> getAllowedChatsSet() {
        Set<Long> allowedChats = new HashSet<>();
        for (var doc : chats.find()) {
            allowedChats.add(doc.getLong("chatId"));
        }
        return allowedChats;
    }

    @Override
    public void updateChatId(long oldChatId, long newChatId) {
        chats.updateOne(eq("chatId", oldChatId),
                new Document("$set", new Document("chatId", newChatId)));
    }

    @Override
    public void addAllowedChat(long chatId, String title) {
        chats.insertOne(new Document("chatId", chatId)
                .append("title", title));
    }

    @Override
    public void updateTitle(long chatId, String title) {
        var commit = new Document("title", title);
        chats.updateOne(eq("chatId", chatId), new Document("$set", commit));
    }

    @Override
    public void removeAllowedChat(long chatId) {
        chats.deleteOne(eq("chatId", chatId));
        getChatMembersCollection(chatId).drop();
    }

    @Override
    public Map<Long, String> getAllowedChats() {
        Map<Long, String> chats = new HashMap<>();
        for (var doc : this.chats.find()) {
            chats.put(doc.getLong("chatId"), doc.getString("title"));
        }
        return chats;
    }

    @Override
    public void cleanup() {
        for (var chat : chatMembersDB.listCollectionNames()) {
            if (chats.find(eq("chatId", Long.parseLong(chat))).first() == null)
                getChatMembersCollection(Long.parseLong(chat)).drop();
        }
    }

    @Override
    public boolean pairExistsToday(long chatId) {
        var doc = chats.find(eq("chatId", chatId)).first();
        if (doc == null)
            return false;

        var dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(timeZone);
        var date = Calendar.getInstance(timeZone).getTime();
        var today = dateFormat.format(date);

        if (!doc.containsKey("date") || doc.getLong("date") < Long.parseLong(today))
            return false;

        var hoursFormat = new SimpleDateFormat("HH");
        hoursFormat.setTimeZone(timeZone);
        var hours = Integer.parseInt(hoursFormat.format(date));
        hours = (hours >= 0 && hours < 12) ? 0 : 12;
        return doc.getInteger("hours") == hours;
    }

    @Override
    public void setPair(long chatId, String pair) {
        var history = getPairsHistory(chatId);
        history = (history == null) ? pair : pair + "\n" + history.lines().limit(9).collect(Collectors.joining("\n"));
        var date = Calendar.getInstance(timeZone).getTime();
        var dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(timeZone);

        var commit = new Document("pair", pair).append("history", history);
        var hoursFormat = new SimpleDateFormat("HH");
        hoursFormat.setTimeZone(timeZone);
        var hours = Integer.parseInt(hoursFormat.format(date));
        hours = (hours >= 0 && hours < 12) ? 0 : 12;
        commit.append("date", Long.parseLong(dateFormat.format(date)))
                .append("hours", hours);

        if (chats.find(eq("chatId", chatId)).first() == null) {
            commit.append("chatId", chatId);
            chats.insertOne(commit);
        } else
            chats.updateOne(eq("chatId", chatId), new Document("$set", commit));
    }

    @Override
    public String getPairOfTheDay(long chatId) {
        var doc = chats.find(eq("chatId", chatId)).first();
        return (doc != null) ? doc.getString("pair") : null;
    }

    @Override
    public String getPairsHistory(long chatId) {
        var doc = chats.find(eq("chatId", chatId)).first();
        return (doc != null) ? doc.getString("history") : null;
    }

}