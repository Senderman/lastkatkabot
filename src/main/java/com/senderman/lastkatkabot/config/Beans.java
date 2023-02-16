package com.senderman.lastkatkabot.config;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.senderman.lastkatkabot.BotHandler;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.genshin.Item;
import com.senderman.lastkatkabot.handler.TempCommandRegistryImpl;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.io.IOException;
import java.util.List;
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
    public TempCommandRegistryImpl<Role> commandRegistry(
            BotConfig config,
            @NotNull Authority<Role> authority,
            Set<CommandExecutor> commands,
            Set<CallbackExecutor> callbacks,
            BncTelegramHandler bncTelegramHandler
    ) {
        var registry = new TempCommandRegistryImpl<>(config.username(), authority);

        registry.splitCallbackCommandByWhitespace();

        registry.register(bncTelegramHandler);
        commands.forEach(registry::register);
        callbacks.forEach(registry::register);
        return registry;
    }

    @Singleton
    @Named("love")
    public List<String> love() throws IOException {
        return new YAMLMapper().readValue(getClass().getResourceAsStream("/love.yml"), new TypeReference<>() {
        });
    }

    @Singleton
    @Named("genshinItems")
    public List<Item> genshinItems() throws IOException {
        return new YAMLMapper().readValue(getClass().getResourceAsStream("/genshin/items.yml"), new TypeReference<>() {
        });
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

}
