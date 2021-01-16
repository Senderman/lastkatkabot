package com.senderman.lastkatkabot.bnc;

public class BncResult {

    private final int bulls;
    private final int cows;

    public BncResult(int bulls, int cows) {
        this.bulls = bulls;
        this.cows = cows;
    }

    public int getBulls() {
        return bulls;
    }

    public int getCows() {
        return cows;
    }

    @Override
    public String toString() {
        return bulls + "Б " + cows + "К";
    }
}
