package com.senderman.lastkatkabot.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.Objects;

@MappedEntity("chatUser")
public class ChatUser {

    @Id
    private final String id;
    private final long chatId;
    private final String name;
    private final int lastMessageDate;
    @MappedEntity
    private long userId;

    @Creator
    public ChatUser(long chatId, long userId, String name, int lastMessageDate) {
        this.id = generateId(chatId, userId);
        this.chatId = chatId;
        this.userId = userId;
        this.name = name;
        this.lastMessageDate = lastMessageDate;
    }

    public String getId() {
        return id;
    }

    public static String generateId(long chatId, long userId) {
        return chatId + " " + userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public int getLastMessageDate() {
        return lastMessageDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatUser chatUser = (ChatUser) o;
        return userId == chatUser.userId && chatId == chatUser.chatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, chatId);
    }

    @Override
    public String toString() {
        return "ChatUser{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", chatId=" + chatId +
                ", name='" + name + '\'' +
                ", lastMessageDate=" + lastMessageDate +
                '}';
    }
}
