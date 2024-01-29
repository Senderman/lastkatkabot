package com.senderman.lastkatkabot.feature.l10n.service;

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;

@Singleton
public class L10nService extends ResourceBundleLocalizationService {

    private final UserStatsService users;

    public L10nService(UserStatsService users) {
        super("locale/lang");
        this.users = users;
    }

    public String getLocale(long userId, String name, String locale) {
        return users.findById(userId, name, locale).getLocale();
    }
}
