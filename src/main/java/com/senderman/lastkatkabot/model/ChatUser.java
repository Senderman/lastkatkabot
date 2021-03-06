package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

import java.util.Objects;

@TypeAlias("chatuser")
public class ChatUser {

    @Id
    private String id;

    private long userId;
    private long chatId;
    private int lastMessageDate;

    public ChatUser() {

    }

    public ChatUser(long chatId, long userId) {
        this.id = generateId(chatId, userId);
        this.chatId = chatId;
        this.userId = userId;
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

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public int getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(int lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
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
               "userId=" + userId +
               ", chatId=" + chatId +
               '}';
    }
}
