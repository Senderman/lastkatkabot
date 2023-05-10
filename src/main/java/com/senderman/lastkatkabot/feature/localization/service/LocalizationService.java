package com.senderman.lastkatkabot.feature.localization.service;

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;

@Singleton
public class LocalizationService extends ResourceBundleLocalizationService {

    private final UserStatsService users;

    public LocalizationService(UserStatsService users) {
        super("locale/lang");
        this.users = users;
    }

    public String getLocale(long userId) {
        return users.findById(userId).getLocale();
    }
}
