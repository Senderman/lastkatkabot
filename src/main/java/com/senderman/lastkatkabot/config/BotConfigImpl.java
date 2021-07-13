package com.senderman.lastkatkabot.config;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BotConfigImpl implements BotConfig {

    private final String token;
    private final String username;
    private final String timezone;
    private final long mainAdminId;
    private final long feedbackChannelId;
    private final long notificationChannelId;
    private final String bncHelpPictureId;
    private final String helloGifId;
    private final String leaveStickerId;
    private final String databaseConnection;
    private final String database;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BotConfigImpl(
            @JsonProperty("login") String login,
            @JsonProperty("timezone") String timezone,
            @JsonProperty("mainAdminId") long mainAdminId,
            @JsonProperty("feedbackChannelId") long feedbackChannelId,
            @JsonProperty("notificationChannelId") long notificationChannelId,
            @JsonProperty("bncHelpPictureId") String bncHelpPictureId,
            @JsonProperty("helloGifId") String helloGifId,
            @JsonProperty("leaveStickerId") String leaveStickerId,
            @JsonProperty("databaseConnection") String databaseConnection,
            @JsonProperty("database") String database
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
        this.databaseConnection = databaseConnection;
        this.database = database;
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

    @Override
    public String databaseConnection() {
        return databaseConnection;
    }

    @Override
    public String database() {
        return database;
    }
}
