package com.senderman.anitrackerbot;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MongoDBService implements DBService {

    private final MongoClient client = MongoClients.create(System.getenv("database"));
    private final MongoDatabase database = client.getDatabase("anime");

    private MongoCollection<Document> user(int id) {
        return database.getCollection(String.valueOf(id));
    }

    @Override
    public void saveAnime(int id, int userId, String url) {
        var doc = new Document("id", id).append("url", url);
        user(userId).insertOne(doc);
    }

    @Override
    public String getAnimeUrl(int id, int userId) {
        return Objects.requireNonNull(user(userId).find(Filters.eq("id", id)).first()).getString("url");
    }

    @Override
    public void deleteAnime(int id, int userId) {
        user(userId).deleteOne(Filters.eq("id", id));
    }

    @Override
    public void dropUser(int userId) {
        user(userId).drop();
    }

    @Override
    public int totalAnimes(int userId) {
        return (int) user(userId).countDocuments();
    }

    @Override
    public boolean idExists(int id, int userId) {
        return user(userId).find(Filters.eq("id", id)).first() != null;
    }

    @Override
    public boolean urlExists(String url, int userId) {
        return user(userId).find(Filters.eq("url", url)).first() != null;
    }

    @Override
    public Map<Integer, String> getAllAnimes(int userId) {
        var user = user(userId).find();
        Map<Integer, String> result = new HashMap<>();
        for (var doc : user) {
            result.put(doc.getInteger("id"), doc.getString("url"));
        }
        return result;
    }
}
