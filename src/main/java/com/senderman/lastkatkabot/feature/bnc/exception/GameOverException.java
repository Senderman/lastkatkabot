package com.senderman.lastkatkabot.feature.bnc.exception;

import com.senderman.lastkatkabot.feature.bnc.BncResult;

public class GameOverException extends RuntimeException {

    private final BncResult result;
    private final String answer;

    public GameOverException(BncResult result, String answer) {
        super("No attempts left");
        this.result = result;
        this.answer = answer;
    }

    public BncResult getResult() {
        return result;
    }

    public String getAnswer() {
        return answer;
    }
}
