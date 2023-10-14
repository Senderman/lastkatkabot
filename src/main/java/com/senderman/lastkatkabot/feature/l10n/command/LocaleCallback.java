package com.senderman.lastkatkabot.feature.l10n.command;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class LocaleCallback implements CallbackExecutor {

    public static final String NAME = "LOCALE";
    private final UserStatsService users;
    private final L10nService l;

    public LocaleCallback(UserStatsService users, L10nService localizationService) {
        this.users = users;
        this.l = localizationService;
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

        var answer = l.getString("localization.setlocale.notify", locale);

        ctx.editMessage(answer).callAsync(ctx.sender);
        ctx.answer(answer).callAsync(ctx.sender);
    }
}