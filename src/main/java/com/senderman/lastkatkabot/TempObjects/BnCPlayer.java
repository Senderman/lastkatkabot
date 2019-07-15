package com.senderman.lastkatkabot.TempObjects;

import org.jetbrains.annotations.NotNull;

public class BnCPlayer extends TgUser implements Comparable<BnCPlayer> {

    private final int score;

    public BnCPlayer(int id, String name, int score) {
        super(id, name);
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(@NotNull BnCPlayer bnCPlayer) {
        return this.score - bnCPlayer.score;
    }
}
