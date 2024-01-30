package com.senderman.lastkatkabot.config;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("bot")
public interface BotConfig {

    String getToken();

    String getUsername();

    String getTimezone();

    long getMainAdminId();

    long getNotificationChannelId();

    LocaleConfig getLocale();

    @ConfigurationProperties("locale")
    interface LocaleConfig {

        String getAdminLocale();

        String getDefaultLocale();

        List<String> getSupportedLocales();

    }

}
