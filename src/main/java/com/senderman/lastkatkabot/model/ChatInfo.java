package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;

public class ChatInfo {

    @Id
    private long chatId;

    public ChatInfo() {

    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
