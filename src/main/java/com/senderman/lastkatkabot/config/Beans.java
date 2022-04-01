package com.senderman.lastkatkabot.config;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.senderman.lastkatkabot.BotHandler;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.genshin.Item;
import com.senderman.lastkatkabot.service.fileupload.TelegramFileUploadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Configuration
public class Beans {

    public Beans(ChatUserService chatUserService) {
    }

    @Bean
    public DefaultBotOptions botOptions() {
        var options = new DefaultBotOptions();
        options.setAllowedUpdates(List.of("message", "callback_query"));
        return options;
    }

    @Bean
    public List<String> love() {
        try {
            return new YAMLMapper().readValue(getClass().getResourceAsStream("/love.yml"), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Scope("prototype")
    public List<Item> genshinItems() {
        try {
            return new YAMLMapper().readValue(getClass().getResourceAsStream("/genshin/items.yml"), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public Consumer<Long> chatPolicyViolationConsumer(BotHandler handler) {
        return (chatId) -> {
            Methods.sendMessage(chatId, "📛 Ваш чат в списке спамеров! Бот не хочет здесь работать!").callAsync(handler);
            Methods.leaveChat(chatId).callAsync(handler);
        };
    }

    @Bean
    public TelegramFileUploadService telegramFileUploadService(BotConfig config) {
        return new Retrofit.Builder()
                .baseUrl("https://api.telegram.org/bot" + config.token() + "/")
                .build()
                .create(TelegramFileUploadService.class);
    }

}
