package com.senderman.lastkatkabot.config;

public interface BotConfig {

    String token();

    String username();

    String timezone();

    long mainAdminId();

    long feedbackChannelId();

    long notificationChannelId();

    String bncHelpPictureId();

    String helloGifId();

    String leaveStickerId();

}
