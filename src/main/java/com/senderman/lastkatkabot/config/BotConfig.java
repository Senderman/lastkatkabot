package com.senderman.lastkatkabot.config;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("bot")
public record BotConfig(
        String token,
        String username,
        String timezone,
        long mainAdminId,
        long notificationChannelId,
        LocaleConfig locale
) {

    @ConfigurationProperties("locale")
    public record LocaleConfig(
            String adminLocale,
            String defaultLocale,
            List<String> supportedLocales
    ) {
    }

}
