package com.senderman.lastkatkabot.feature.genshin.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Embeddable;
import io.micronaut.data.annotation.EmbeddedId;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.util.Objects;

@MappedEntity("GENSHIN_CHAT_USER")
public class GenshinChatUser {

    @EmbeddedId
    private final PrimaryKey primaryKey;
    private int lastRollDate;
    private int fourPity;
    private int fivePity;

    public GenshinChatUser(long chatId, long userId) {
        this.primaryKey = new PrimaryKey(chatId, userId);

    }

    @Creator
    public GenshinChatUser(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public int getLastRollDate() {
        return lastRollDate;
    }

    public void setLastRollDate(int lastRollDate) {
        this.lastRollDate = lastRollDate;
    }

    public int getFourPity() {
        return fourPity;
    }

    public void setFourPity(int fourPity) {
        this.fourPity = fourPity;
    }

    public int getFivePity() {
        return fivePity;
    }

    public void setFivePity(int fivePity) {
        this.fivePity = fivePity;
    }

    public void incFourPity() {
        fourPity++;
    }

    public void incFivePity() {
        fivePity++;
    }

    @Embeddable
    public static class PrimaryKey {

        @MappedProperty("chat_id")
        private final long chatId;

        @MappedProperty("user_id")
        private final long userId;

        public PrimaryKey(long chatId, long userId) {
            this.chatId = chatId;
            this.userId = userId;
        }

        public long getChatId() {
            return chatId;
        }

        public long getUserId() {
            return userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrimaryKey that = (PrimaryKey) o;
            return chatId == that.chatId && userId == that.userId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chatId, userId);
        }
    }
}
