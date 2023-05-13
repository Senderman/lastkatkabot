package com.senderman.lastkatkabot.feature.l10n.service;

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;

@Singleton
public class L10nService extends ResourceBundleLocalizationService {

    public static final String DEFAULT_LOCALE = "ru";
    private final UserStatsService users;

    public L10nService(UserStatsService users) {
        super("locale/lang");
        this.users = users;
    }

    public String getLocale(long userId) {
        return users.findById(userId).getLocale();
    }
}
