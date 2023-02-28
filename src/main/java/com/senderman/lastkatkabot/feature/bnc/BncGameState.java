package com.senderman.lastkatkabot.feature.bnc;

import java.util.List;

public record BncGameState(
        long id,
        long creatorId,
        int length,
        List<BncResult> history,
        int attemptsLeft,
        long startTime,
        boolean isHexadecimal,
        String answer) {
}
