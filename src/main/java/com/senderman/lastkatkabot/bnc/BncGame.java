package com.senderman.lastkatkabot.bnc;

import com.senderman.lastkatkabot.model.BncGameSave;
import com.senderman.lastkatkabot.service.Serializer;
import com.senderman.lastkatkabot.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class BncGame {

    private final long chatId;
    private final String answer;
    private final int length;
    private final List<Pair<String, Integer>> history;
    private final Set<Integer> checkedNumbers;
    // true if user can't run check() with already checked digit. Default false
    private boolean allowRepeatingChecking;
    private int attemptsLeft;

    public BncGame(long chatId, int length) {
        this.chatId = chatId;
        this.length = length;
        this.history = new ArrayList<>();
        this.checkedNumbers = new HashSet<>();
        this.answer = generateAnswer(length);
        this.attemptsLeft = (int) (length * 2.5);
        allowRepeatingChecking = false;
    }

    public void allowRepeatingChecking() {
        this.allowRepeatingChecking = true;
    }

    public void disallowRepeatingChecking() {
        this.allowRepeatingChecking = false;
    }

    public BncGameSave getSave(Serializer serializer) {
        return new BncGameSave(chatId, serializer.serialize(this));
    }

    private String generateAnswer(int length) {
        return ThreadLocalRandom.current().ints(0, 10)
                .distinct()
                .limit(length)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining());
    }

}
