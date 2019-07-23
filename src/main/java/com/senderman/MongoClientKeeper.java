package com.senderman;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoClientKeeper {

    private static MongoClient client = MongoClients.create(System.getenv("database"));

    public static MongoClient getClient() {
        return client;
    }
}
