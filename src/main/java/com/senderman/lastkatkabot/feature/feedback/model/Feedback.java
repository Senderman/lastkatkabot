package com.senderman.lastkatkabot.feature.feedback.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.util.Objects;

@MappedEntity("FEEDBACK")
public class Feedback {

    @MappedProperty("message")
    private final String message;
    @MappedProperty("user_id")
    private final long userId;
    @MappedProperty("user_name")
    private final String userName;
    @MappedProperty("chat_id")
    private final long chatId;
    @MappedProperty("chat_title")
    @Nullable
    private final String chatTitle;
    @MappedProperty("message_id")
    private final int messageId;
    @Id
    @GeneratedValue
    @MappedProperty("id")
    private int id;
    @MappedProperty("replied")
    private boolean replied;

    @Creator
    public Feedback(String message, long userId, String userName, long chatId, @Nullable String chatTitle, int messageId) {
        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        this.messageId = messageId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public long getChatId() {
        return chatId;
    }

    @Nullable
    public String getChatTitle() {
        return chatTitle;
    }

    public int getMessageId() {
        return messageId;
    }

    public boolean isReplied() {
        return replied;
    }

    public void setReplied(boolean replied) {
        this.replied = replied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feedback feedback = (Feedback) o;
        return id == feedback.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
