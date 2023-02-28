package com.senderman.lastkatkabot.feature.bnc;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record BncResult(String number, int bulls, int cows, int attempts) {

    @JsonIgnore
    public boolean isWin() {
        return bulls == number.length();
    }

    @JsonIgnore
    public boolean isGameOver() {
        return attempts <= 0;
    }
}
