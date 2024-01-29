package com.senderman.lastkatkabot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.senderman.lastkatkabot.feature.genshin.model.Item;
import com.senderman.lastkatkabot.feature.love.model.Love;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.io.IOException;
import java.util.HashMap;
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
    public Love love(BotConfig config) throws IOException {
        var typeRef = new TypeReference<List<String>>() {
        };
        var mapper = new YAMLMapper();
        String basePath = "/love/";
        var result = new HashMap<String, List<String>>();
        for (var locale : config.getLocale().getSupportedLocales()) {
            var value = mapper.readValue(getClass().getResourceAsStream(basePath + locale + ".yml"), typeRef);
            result.put(locale, value);
        }
        return new Love(result, config);
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
