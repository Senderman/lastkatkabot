package com.senderman.lastkatkabot.bnc;

public record BncResult(String number, int bulls, int cows, int attempts) {

    public boolean isWin() {
        return bulls == number.length();
    }

    public boolean isGameOver() {
        return attempts <= 0;
    }
}
