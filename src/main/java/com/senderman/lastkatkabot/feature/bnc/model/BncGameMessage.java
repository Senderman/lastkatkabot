package com.senderman.lastkatkabot.feature.bnc.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.*;

import java.util.Objects;

@MappedEntity("bnc_game_message")
public class BncGameMessage {

    @EmbeddedId
    private final PrimaryKey primaryKey;

    @Creator
    public BncGameMessage(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public BncGameMessage(long gameId, int messageId) {
        this.primaryKey = new PrimaryKey(gameId, messageId);
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    @Transient
    public long getGameId() {
        return primaryKey.getGameId();
    }

    @Transient
    public int getMessageId() {
        return primaryKey.getMessageId();
    }


    @Embeddable
    public static class PrimaryKey {

        @MappedProperty("game_id")
        private final long gameId;

        @MappedProperty("message_id")
        private final int messageId;

        public PrimaryKey(long gameId, int messageId) {
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
            PrimaryKey that = (PrimaryKey) o;
            return gameId == that.gameId && messageId == that.messageId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(gameId, messageId);
        }
    }
}
