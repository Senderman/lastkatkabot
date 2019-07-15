package com.senderman.lastkatkabot.TempObjects;

public class BnCPlayer extends TgUser {

    private final int score;

    public BnCPlayer(int id, String name, int score) {
        super(id, name);
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
