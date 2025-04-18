package com.senderman.lastkatkabot.util;

import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;
import java.util.Set;

@Singleton
public class TelegramUsersHelper {

    private final BotConfig botConfig;
    private final Set<Long> telegramServiceUserIds;
    private final UserStatsService userStatsService;

    public TelegramUsersHelper(BotConfig config, UserStatsService userStatsService) {
        this.botConfig = config;
        this.userStatsService = userStatsService;
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
     * Checks if the provided {@code user} is a different bot
     *
     * @param user a telegram user object
     * @return true if it's another bot, false if it's not a bot, or it's my bot
     */
    public boolean isAnotherBot(@NotNull User user) {
        return user.getIsBot() && !Objects.equals(user.getUserName(), botConfig.username());
    }

    /**
     * Get the user's name within {@link User} object by userId
     * It tries to find user's name in CHAT_USERS table. If it's empty or blank, it will have the default name
     *
     * @param userId id of the user
     * @param ctx    localized message context
     * @return {@link User} object with user's firstName
     */
    public User findUserFirstName(long userId, L10nMessageContext ctx) {
        var user = userStatsService.findById(userId);
        if (user.getName().isBlank()) {
            user.setName(ctx.getString("common.noName"));
        }
        return new User(user.getUserId(), user.getName(), false);
    }
}
