package com.senderman.lastkatkabot.bnc;

import java.util.List;

// TODO make record
public class BncGameState {
    private final long id;
    private final long creatorId;
    private final int length;
    private final List<BncResult> history;
    private final int attemptsLeft;
    private final long startTime;
    private final boolean isHexadecimal;
    private final String answer;

    public BncGameState(
            long id,
            long creatorId,
            int length,
            List<BncResult> history,
            int attemptsLeft,
            long startTime,
            boolean isHexadecimal,
            String answer
    ) {
        this.id = id;
        this.creatorId = creatorId;
        this.length = length;
        this.history = history;
        this.attemptsLeft = attemptsLeft;
        this.startTime = startTime;
        this.isHexadecimal = isHexadecimal;
        this.answer = answer;
    }

    public long getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public List<BncResult> getHistory() {
        return history;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isHexadecimal() {
        return isHexadecimal;
    }

    public String getAnswer() {
        return answer;
    }

    public long getCreatorId() {
        return creatorId;
    }
}
