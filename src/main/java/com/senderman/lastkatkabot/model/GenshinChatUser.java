package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("genshinChatUser")
public class GenshinChatUser {

    @Id
    private String id;
    private long chatId;
    private long userid;
    private String lastRollDate;
    private int fourPity;
    private int fivePity;

    public GenshinChatUser(long chatId, long userid) {
        this.id = generateId(chatId, userid);
        this.chatId = chatId;
        this.userid = userid;

    }

    public static String generateId(long chatId, long userId) {
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

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public String getLastRollDate() {
        return lastRollDate;
    }

    public void setLastRollDate(String lastRollDate) {
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
