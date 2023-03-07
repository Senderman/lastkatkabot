package com.senderman.lastkatkabot.util;

import com.senderman.lastkatkabot.config.BotConfig;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;
import java.util.Set;

@Singleton
public class TelegramUsersHelper {

    private final BotConfig botConfig;
    private final Set<Long> telegramServiceUserIds;

    public TelegramUsersHelper(BotConfig config) {
        this.botConfig = config;
        telegramServiceUserIds = Set.of(
                777000L, // attached channel's messages
                1087968824L, // anonymous group admin @GroupAnonymousBot
                136817688L // Channel message, @Channel_Bot
        );
    }

    public boolean isServiceUserId(@NotNull User user) {
        return isServiceUserId(user.getId());
    }

    public boolean isServiceUserId(@NotNull Long userId) {
        return telegramServiceUserIds.contains(userId);
    }

    /**
     * Checks if the provided {@code user} is a bot with a username equals to the provided in the bot config
     *
     * @param user a telegram user object
     * @return true if it's my bot, false otherwise
     */
    public boolean isMyBot(@NotNull User user) {
        return user.getIsBot() && Objects.equals(user.getUserName(), botConfig.getUsername());
    }

    /**
     * Checks if the provided {@code user} is a different bot
     *
     * @param user a telegram user object
     * @return true if it's another bot, false if it's not a bot, or it's my bot
     */
    public boolean isAnotherBot(@NotNull User user) {
        return user.getIsBot() && !Objects.equals(user.getUserName(), botConfig.getUsername());
    }
}
