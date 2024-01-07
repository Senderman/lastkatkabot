package com.senderman.lastkatkabot.feature.bnc.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.util.Objects;

@MappedEntity("BNC_GAME_MESSAGE")
public class BncGameMessage {

    @MappedProperty("game_id")
    private final long gameId;
    @MappedProperty("message_id")
    private final int messageId;
    @Id
    @GeneratedValue
    @MappedProperty("id")
    private long id;

    @Creator
    public BncGameMessage(long gameId, int messageId) {
        this.gameId = gameId;
        this.messageId = messageId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
