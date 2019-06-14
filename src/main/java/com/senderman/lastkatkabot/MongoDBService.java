package com.senderman.lastkatkabot;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.senderman.lastkatkabot.TempObjects.BullsAndCowsGame;
import com.senderman.lastkatkabot.TempObjects.TgUser;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDBService implements DBService {
    private final TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
    private final MongoClient client = MongoClients.create(System.getenv("database"));
    private final MongoDatabase lastkatkaDB = client.getDatabase("lastkatka");
    private final MongoDatabase chatMembersDB = client.getDatabase("chatmembers");
    private final MongoCollection<Document> admins = lastkatkaDB.getCollection("admins");
    private final MongoCollection<Document> premiumUsers = lastkatkaDB.getCollection("premium");
    private final MongoCollection<Document> blacklist = lastkatkaDB.getCollection("blacklist");
    private final MongoCollection<Document> userstats = lastkatkaDB.getCollection("userstats");
    private final MongoCollection<Document> settings = lastkatkaDB.getCollection("settings");
    private final MongoCollection<Document> bncgames = lastkatkaDB.getCollection("bncgames");
    private final MongoCollection<Document> allowedchats = lastkatkaDB.getCollection("allowedchats");

    private MongoCollection<Document> getChatMembersCollection(long chatId) {
        return chatMembersDB.getCollection(String.valueOf(chatId));
    }

    private MongoCollection<Document> getUsersCollection(COLLECTION_TYPE type) {
        switch (type) {
            case ADMINS:
                return admins;
            case PREMIUM:
                return premiumUsers;
            case BLACKLIST:
                return blacklist;
        }
        return null; // never used
    }

    public void initStats(int id) {
        var doc = new Document("id", id)
                .append("total", 0)
                .append("wins", 0)
                .append("bncwins", 0);
        userstats.insertOne(doc);
    }

    public void incDuelWins(int id) {
        var doc = userstats.find(Filters.eq("id", id)).first();
        if (doc == null)
            initStats(id);

        var updateDoc = new Document()
                .append("$inc", new Document("wins", 1).append("total", 1));

        userstats.updateOne(Filters.eq("id", id), updateDoc);
    }

    public void incTotalDuels(int id) {
        var doc = userstats.find(Filters.eq("id", id)).first();
        if (doc == null)
            initStats(id);

        var updateDoc = new Document()
                .append("$inc", new Document("total", 1));
        userstats.updateOne(Filters.eq("id", id), updateDoc);
    }

    @Override
    public void incBNCWins(int id, int points) {
        var doc = userstats.find(Filters.eq("id", id)).first();
        if (doc == null)
            initStats(id);

        var updateDoc = new Document()
                .append("$inc", new Document("bnc", points));
        userstats.updateOne(Filters.eq("id", id), updateDoc);
    }

    public Map<String, Integer> getStats(int id) {
        int total = 0, wins = 0, bncwins = 0;
        var doc = userstats.find(Filters.eq("id", id)).first();
        if (doc == null) {
            initStats(id);
        } else {
            total = doc.getInteger("total");
            wins = doc.getInteger("wins");
            bncwins = doc.getInteger("bnc");
        }
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("wins", wins);
        stats.put("bnc", bncwins);
        return stats;
    }

    @Override
    public void setUserLocale(int id, String locale) {
        var doc = userstats.find(Filters.eq("id", id)).first();
        if (doc == null)
            initStats(id);
        userstats.updateOne(Filters.eq("id", id),
                new Document("$set", new Document("locale", locale)));
    }

    @Override
    public String getUserLocale(int id) {
        var doc = userstats.find(Filters.eq("id", id)).first();
        if (doc == null) {
            initStats(id);
            return "en";
        }
        var locale = doc.getString("locale");
        return (locale != null) ? locale : "en";
    }

    @Override
    public void addTgUser(int id, String name, COLLECTION_TYPE type) {
        var collection = Objects.requireNonNull(getUsersCollection(type));
        if (collection.find(Filters.eq("id", id)).first() == null)
            collection.insertOne(new Document("id", id)
                    .append("name", name));

    }

    @Override
    public void removeTGUser(int id, COLLECTION_TYPE type) {
        Objects.requireNonNull(getUsersCollection(type)).deleteOne(Filters.eq("id", id));
    }

    @Override
    public Set<TgUser> getTgUsers(COLLECTION_TYPE collection_type) {
        Set<TgUser> result = new HashSet<>();
        var collection = Objects.requireNonNull(getUsersCollection(collection_type));
        for (var doc : collection.find()) {
            result.add(new TgUser(doc.getInteger("id"), doc.getString("name")));
        }
        return result;
    }

    @Override
    public Set<Integer> getTgUsersIds(COLLECTION_TYPE collection_type) {
        Set<Integer> result = new HashSet<>();
        var collection = Objects.requireNonNull(getUsersCollection(collection_type));
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

        var doc = chat.find(Filters.eq("id", user.getId())).first();
        if (doc == null) {
            commit.append("id", user.getId());
            chat.insertOne(commit);
        } else
            chat.updateOne(Filters.eq("id", user.getId()), new Document("$set", commit));
    }

    @Override
    public void removeUserFromChatDB(int userId, long chatId) {
        getChatMembersCollection(chatId).deleteOne(Filters.eq("id", userId));
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
    public void removeOldUsers(long chatId, long date) {
        var chat = getChatMembersCollection(chatId);
        chat.deleteMany(Filters.lt("lastMessageDate", date));
    }

    @Override
    public void setChatLocale(long chatId, String locale) {
        var doc = allowedchats.find(Filters.eq("chatId", chatId)).first();
        if (doc == null) {
            allowedchats.insertOne(new Document("chatId", chatId)
                    .append("locale", locale));
        } else {
            allowedchats.updateOne(Filters.eq("chatId", chatId),
                    new Document("$set", new Document("locale", locale)));
        }
    }

    @Override
    public String getChatLocale(long chatId) {
        var doc = allowedchats.find(Filters.eq("chatId", chatId)).first();
        if (doc == null)
            return "en";
        var locale = doc.getString("locale");
        return (locale != null) ? locale : "en";
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
        var gameSaved = bncgames.find(Filters.eq("chatId", chatId)).first() != null;
        var gson = new Gson();
        var gameAsJson = gson.toJson(game);
        var commit = new Document("game", gameAsJson);
        if (gameSaved) {
            bncgames.updateOne(Filters.eq("chatId", chatId),
                    new Document("$set", commit));
        } else {
            bncgames.insertOne(commit.append("chatId", chatId));
        }
    }

    @Override
    public void deleteBncGame(long chatId) {
        bncgames.deleteOne(Filters.eq("chatId", chatId));
    }

    @Override
    public int getTournamentMessageId() {
        var doc = settings.find(Filters.exists("messageId", true)).first();
        if (doc == null)
            return 0;
        return doc.getInteger("messageId");
    }

    @Override
    public void setTournamentMessage(int messageId) {
        var doc = settings.find(Filters.exists("messageId", true)).first();
        if (doc == null)
            settings.insertOne(new Document("messageId", messageId));
        else
            settings.updateOne(Filters.exists("messageId", true),
                    new Document(
                            "$set", new Document("messageId", messageId)
                    ));
    }

    @Override
    public Set<Long> getAllowedChatsSet() {
        Set<Long> allowedChats = new HashSet<>();
        for (var doc : allowedchats.find()) {
            allowedChats.add(doc.getLong("chatId"));
        }
        return allowedChats;
    }

    @Override
    public void addAllowedChat(long chatId, String title) {
        allowedchats.insertOne(new Document("chatId", chatId)
                .append("title", title));
    }

    @Override
    public void updateTitle(long chatId, String title) {
        var commit = new Document("title", title);
        allowedchats.updateOne(Filters.eq("chatId", chatId), new Document("$set", commit));
    }

    @Override
    public void removeAllowedChat(long chatId) {
        allowedchats.deleteOne(Filters.eq("chatId", chatId));
        getChatMembersCollection(chatId).drop();
    }

    @Override
    public Map<Long, String> getAllowedChats() {
        Map<Long, String> chats = new HashMap<>();
        for (var doc : allowedchats.find()) {
            chats.put(doc.getLong("chatId"), doc.getString("title"));
        }
        return chats;
    }

    @Override
    public boolean pairExistsToday(long chatId) {
        var doc = allowedchats.find(Filters.eq("chatId", chatId)).first();
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

        if (allowedchats.find(Filters.eq("chatId", chatId)).first() == null) {
            commit.append("chatId", chatId);
            allowedchats.insertOne(commit);
        } else
            allowedchats.updateOne(Filters.eq("chatId", chatId), new Document("$set", commit));
    }

    @Override
    public String getPairOfTheDay(long chatId) {
        var doc = allowedchats.find(Filters.eq("chatId", chatId)).first();
        return (doc != null) ? doc.getString("pair") : null;
    }

    @Override
    public String getPairsHistory(long chatId) {
        var doc = allowedchats.find(Filters.eq("chatId", chatId)).first();
        return (doc != null) ? doc.getString("history") : null;
    }
}