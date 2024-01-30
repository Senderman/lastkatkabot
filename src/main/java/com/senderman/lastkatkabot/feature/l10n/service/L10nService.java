package com.senderman.lastkatkabot.feature.l10n.service;

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;

import java.util.Objects;

@Singleton
public class L10nService extends ResourceBundleLocalizationService {

    private final UserStatsService users;
    private final BotConfig config;

    public L10nService(UserStatsService users, BotConfig config) {
        super("locale/lang");
        this.users = users;
        this.config = config;
    }

    public String getLocale(long userId) {
        return Objects.requireNonNullElse(users.findById(userId).getLocale(), config.getLocale().getDefaultLocale());
    }
}
