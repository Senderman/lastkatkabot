package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("bnc")
public class BncGameSave {
    @Id
    private long chatId;
    private String game;

    public BncGameSave() {
    }

    public BncGameSave(long chatId, String game) {
        this.chatId = chatId;
        this.game = game;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }


}
