package com.senderman.lastkatkabot.feature.tracking.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.*;

import java.util.Objects;

@MappedEntity("CHAT_USER")
public class ChatUser {

    @EmbeddedId
    private final PrimaryKey primaryKey;
    private int lastMessageDate;

    @Creator
    public ChatUser(PrimaryKey primaryKey, int lastMessageDate) {
        this.primaryKey = primaryKey;
        this.lastMessageDate = lastMessageDate;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    @Transient
    public long getUserId() {
        return primaryKey.getUserId();
    }

    @Transient
    public long getChatId() {
        return primaryKey.getChatId();
    }

    public int getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(int lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
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
