package com.senderman.lastkatkabot.model;


@TypeAlias("blacklistedChat")
public class BlacklistedChat {
    @Id
    private long chatId;

    public BlacklistedChat(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }
}
