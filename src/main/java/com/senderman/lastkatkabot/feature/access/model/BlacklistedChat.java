package com.senderman.lastkatkabot.feature.access.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

@MappedEntity("blacklisted_chat")
public class BlacklistedChat {

    @Id
    @MappedProperty("chat_id")
    private final long chatId;

    @Creator
    public BlacklistedChat(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }
}
