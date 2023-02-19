package com.senderman.lastkatkabot.genshin.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("genshinChatUser")
public class GenshinChatUser {

    @Id
    private String id;
    private final long chatId;
    private final long userId;
    private int lastRollDate;
    private int fourPity;
    private int fivePity;

    @Creator
    public GenshinChatUser(long chatId, long userId) {
        this.id = generateId(chatId, userId);
        this.chatId = chatId;
        this.userId = userId;
        this.lastRollDate = 0;
        this.fourPity = 0;
        this.fivePity = 0;

    }

    private static String generateId(long chatId, long userId) {
        return chatId + " " + userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public long getUserId() {
        return userId;
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
}
