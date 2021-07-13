package com.senderman.lastkatkabot.model;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Objects;

public class BncGameMessage {

    @BsonId
    private String id;
    private final long gameId;
    private final int messageId;

    public BncGameMessage(long gameId, int messageId) {
        this.gameId = gameId;
        this.messageId = messageId;
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
