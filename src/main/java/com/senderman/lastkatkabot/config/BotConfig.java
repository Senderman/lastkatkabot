package com.senderman.lastkatkabot.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("bot")
public interface BotConfig {

    String getToken();

    String getUsername();

    String getTimezone();

    long getMainAdminId();

    long getFeedbackChannelId();

    long getNotificationChannelId();

    String getBncHelpPictureId();

    String getHelloGifId();

    String getLeaveStickerId();

}
