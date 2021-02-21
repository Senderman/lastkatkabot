package com.senderman.lastkatkabot.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final String databaseName;
    private final String connectionString;

    public MongoConfig(
            @Value("${dbName}") String databaseName,
            @Value("${dbUri}") String connectionString
    ) {
        this.databaseName = databaseName;
        this.connectionString = connectionString;
    }

    @Override
    protected @NotNull String getDatabaseName() {
        return databaseName;
    }

    @Override
    public @NotNull MongoClient mongoClient() {
        return MongoClients.create(connectionString);
    }
}
