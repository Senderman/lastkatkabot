package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("blacklistedChat")
public class BlacklistedChat {
    @Id
    private long chatId;

    public BlacklistedChat() {
    }

    public BlacklistedChat(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }
}
