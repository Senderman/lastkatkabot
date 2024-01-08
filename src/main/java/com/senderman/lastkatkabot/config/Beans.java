package com.senderman.lastkatkabot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.senderman.lastkatkabot.feature.genshin.model.Item;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.io.IOException;
import java.util.List;

@Factory
public class Beans {

    @Singleton
    public DefaultBotOptions botOptions() {
        var options = new DefaultBotOptions();
        options.setAllowedUpdates(List.of("message", "callback_query"));
        return options;
    }

    @Singleton
    @Named("love")
    public List<String> love() throws IOException {
        try (var in = getClass().getResourceAsStream("/love.yml")) {
            return new YAMLMapper().readValue(in, new TypeReference<>() {
            });
        }
    }

    @Singleton
    @Named("genshinItems")
    public List<Item> genshinItems() throws IOException {
        try (var in = getClass().getResourceAsStream("/genshin/items.yml")) {
            return new YAMLMapper().readValue(in, new TypeReference<>() {
            });
        }
    }

    @Singleton
    @Named("messageToJsonMapper")
    public ObjectMapper messageToJsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }
}
