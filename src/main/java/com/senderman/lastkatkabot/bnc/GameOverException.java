package com.senderman.lastkatkabot.bnc;

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
