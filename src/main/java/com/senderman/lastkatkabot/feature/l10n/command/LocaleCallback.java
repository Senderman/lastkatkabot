package com.senderman.lastkatkabot.feature.l10n.command;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class LocaleCallback implements CallbackExecutor {

    public static final String NAME = "LOCALE";
    private final UserStatsService users;

    public LocaleCallback(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(@NotNull L10nCallbackQueryContext ctx) {
        final var userId = ctx.user().getId();
        final var locale = ctx.argument(0);

        var userStats = users.findById(userId);
        userStats.setLocale(locale);
        users.save(userStats);

        ctx.editMessage(ctx.getString("localization.setlocale.notify")).callAsync(ctx.sender);
        ctx.answer(ctx.getString("localization.setlocale.notify")).callAsync(ctx.sender);
    }
}