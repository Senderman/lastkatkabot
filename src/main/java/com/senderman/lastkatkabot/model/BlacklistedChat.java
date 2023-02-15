package com.senderman.lastkatkabot.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("blacklistedChat")
public class BlacklistedChat {

    @Id
    private final long chatId;

    @Creator
    public BlacklistedChat(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }
}
