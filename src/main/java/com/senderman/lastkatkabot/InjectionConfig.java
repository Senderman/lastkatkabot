package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.beans.Config;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.config.BotConfigImpl;
import com.senderman.lastkatkabot.service.CurrentTime;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.service.JsonSerializer;
import com.senderman.lastkatkabot.service.Serializer;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class InjectionConfig extends AbstractModule {

    private final Config config;

    public InjectionConfig(Config config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        final var configLoader = new YamlConfigLoaderService();
        final var configFile = configLoader.configFile("lastkatkabot", config.getProfile());
        final var botConfig = configLoader.loadFile(configFile, BotConfigImpl.class);
        int cpus = Runtime.getRuntime().availableProcessors() - 1;

        bind(Serializer.class).to(JsonSerializer.class);

        bind(ImageService.class);
        bind(CurrentTime.class);


        bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(cpus));
        bind(BotConfig.class).toInstance(botConfig);
        bind(MongoDatabase.class).toInstance(createMongoDbDatabase(botConfig));

        bind(ObjectMapper.class)
                .annotatedWith(Names.named("jsonMapper"))
                .toInstance(new ObjectMapper());
        var yamlMapper = new YAMLMapper();
        try {
            var love = yamlMapper.readValue(getClass().getResourceAsStream("/love.yml"), Love.class);
            bind(Love.class).toInstance(love);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MongoDatabase createMongoDbDatabase(BotConfig config) {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder()
                .automatic(true)
                .build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(config.databaseConnection()))
                .codecRegistry(codecRegistry)
                .build();
        MongoClient client = MongoClients.create(clientSettings);
        return client.getDatabase(config.database());
    }
}
