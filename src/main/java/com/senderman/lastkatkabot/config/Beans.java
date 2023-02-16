package com.senderman.lastkatkabot.config;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.CommandRegistry;
import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.senderman.lastkatkabot.BotHandler;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.genshin.Item;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Factory
public class Beans {

    @Singleton
    public DefaultBotOptions botOptions() {
        var options = new DefaultBotOptions();
        options.setAllowedUpdates(List.of("message", "callback_query"));
        return options;
    }

    @Singleton
    public CommandRegistry<Role> commandRegistry(
            BotConfig config,
            @NotNull Authority<Role> authority,
            Set<CommandExecutor> commands,
            Set<CallbackExecutor> callbacks,
            BncTelegramHandler bncTelegramHandler
    ) {
        var registry = new CommandRegistry<>(config.username(), authority);

        registry.splitCallbackCommandByWhitespace();

        registry.register(bncTelegramHandler);
        commands.forEach(registry::register);
        callbacks.forEach(registry::register);
        return registry;
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

    //@Singleton
    //@Named("chatPolicyViolationConsumer")
    public Consumer<Long> chatPolicyViolationConsumer(BotHandler handler) {
        return (chatId) -> {
            Methods.sendMessage(chatId, "📛 Ваш чат в списке спамеров! Бот не хочет здесь работать!").callAsync(handler);
            Methods.leaveChat(chatId).callAsync(handler);
        };
    }

    @Singleton
    @Named("messageToJsonMapper")
    public ObjectMapper messageToJsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    @Singleton
    public MongoDatabase mongoDatabase(@Value("${mongodb.uri}") String uri) {
        var connectionString = new ConnectionString(uri);
        var client = MongoClients.create(connectionString);
        return client.getDatabase(Objects.requireNonNull(connectionString.getDatabase()));
    }

}
