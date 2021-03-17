package com.senderman.lastkatkabot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SpringBotConfig implements BotConfig {

    private final String token;
    private final String username;
    private final String timezone;
    private final long mainAdminId;
    private final long feedbackChannelId;
    private final long notificationChannelId;
    private final String bncHelpPictureId;
    private final String helloGifId;
    private final String leaveStickerId;

    public SpringBotConfig(
            @Value("${login}") String login,
            @Value("${timezone}") String timezone,
            @Value("${mainAdminId}") long mainAdminId,
            @Value("${feedbackChannelId}") long feedbackChannelId,
            @Value("${notificationChannelId}") long notificationChannelId,
            @Value("${bncHelpPictureId}") String bncHelpPictureId,
            @Value("${helloGifId}") String helloGifId,
            @Value("${leaveStickerId}") String leaveStickerId
    ) {

        var loginArgs = login.split("\\s+");
        this.username = loginArgs[0];
        this.token = loginArgs[1];
        this.timezone = timezone;
        this.mainAdminId = mainAdminId;
        this.feedbackChannelId = feedbackChannelId;
        this.notificationChannelId = notificationChannelId;
        this.bncHelpPictureId = bncHelpPictureId;
        this.helloGifId = helloGifId;
        this.leaveStickerId = leaveStickerId;
    }

    @Override
    public String token() {
        return token;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String timezone() {
        return timezone;
    }

    @Override
    public long mainAdminId() {
        return mainAdminId;
    }

    @Override
    public long feedbackChannelId() {
        return feedbackChannelId;
    }

    @Override
    public long notificationChannelId() {
        return notificationChannelId;
    }

    @Override
    public String bncHelpPictureId() {
        return bncHelpPictureId;
    }

    @Override
    public String helloGifId() {
        return helloGifId;
    }

    @Override
    public String leaveStickerId() {
        return leaveStickerId;
    }
}
