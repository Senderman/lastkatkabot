package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;


@TypeAlias("bncfloodmessage")
public class BncFloodMessage {
    @Id
    private String id;
    private int messageId;
    private long gameId;

    public BncFloodMessage() {
    }

    public BncFloodMessage(int messageId, long gameId) {
        this.id = generateId(gameId, messageId);
        this.messageId = messageId;
        this.gameId = gameId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static String generateId(long gameId, int messageId) {
        return gameId + " " + messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }
}
