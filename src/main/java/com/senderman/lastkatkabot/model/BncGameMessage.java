package com.senderman.lastkatkabot.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.Objects;

@MappedEntity("bncGameMessage")
public class BncGameMessage {

    @Id
    @GeneratedValue
    private String id;
    private final long gameId;
    private final int messageId;

    @Creator
    public BncGameMessage(long gameId, int messageId) {
        this.gameId = gameId;
        this.messageId = messageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getGameId() {
        return gameId;
    }

    public int getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BncGameMessage that = (BncGameMessage) o;
        return gameId == that.gameId && messageId == that.messageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, messageId);
    }
}
