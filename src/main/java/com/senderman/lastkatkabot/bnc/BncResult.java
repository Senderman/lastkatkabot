package com.senderman.lastkatkabot.bnc;

public class BncResult {

    private final String number;
    private final int bulls;
    private final int cows;
    private final int attempts;

    public BncResult(String number, int bulls, int cows, int attempts) {
        this.number = number;
        this.bulls = bulls;
        this.cows = cows;
        this.attempts = attempts;
    }

    public String getNumber() {
        return number;
    }

    public int getBulls() {
        return bulls;
    }

    public int getCows() {
        return cows;
    }

    public int getAttempts() {
        return attempts;
    }

    public boolean isWin() {
        return bulls == number.length();
    }

    public boolean isGameOver() {
        return attempts <= 0;
    }
}
