package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

import java.util.Objects;

@TypeAlias("chatuser")
public class ChatUser {

    @Id
    private String id;

    private int userId;
    private long chatId;

    public ChatUser() {

    }

    public ChatUser(int userId, long chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
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
