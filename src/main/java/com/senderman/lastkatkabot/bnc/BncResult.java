package com.senderman.lastkatkabot.bnc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BncResult {

    private final String number;
    private final int bulls;
    private final int cows;
    private final int attempts;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BncResult(
            @JsonProperty("number") String number,
            @JsonProperty("bulls") int bulls,
            @JsonProperty("cows") int cows,
            @JsonProperty("attempts") int attempts
    ) {
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

    @JsonIgnore
    public boolean isWin() {
        return bulls == number.length();
    }

    @JsonIgnore
    public boolean isGameOver() {
        return attempts <= 0;
    }
}
