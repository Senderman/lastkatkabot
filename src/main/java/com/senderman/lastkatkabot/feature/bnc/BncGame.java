package com.senderman.lastkatkabot.feature.bnc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.senderman.lastkatkabot.feature.bnc.exception.*;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameState;
import com.senderman.lastkatkabot.feature.bnc.model.BncResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BncGame {

    private final long id;
    private final long creatorId;
    private final String answer;
    private final int length;
    private final List<BncResult> history;
    private final Set<String> checkedNumbers;
    private final long startTime;
    private final boolean isHexadecimal;
    private int attemptsLeft;

    public BncGame(long id, long creatorId, int length, boolean isHexadecimal) {
        this.id = id;
        this.creatorId = creatorId;
        if (length < 4 || length > (isHexadecimal ? 16 : 10)) {
            throw new IllegalArgumentException("Wrong length " + length + ". 4..10 for DEC and 4..16 for HEX");
        }
        this.length = length;
        this.isHexadecimal = isHexadecimal;
        this.history = new ArrayList<>();
        this.checkedNumbers = new HashSet<>();
        this.answer = generateAnswer(length);
        this.attemptsLeft = totalAttempts(length, isHexadecimal);
        this.startTime = System.currentTimeMillis();
    }

    @JsonCreator
    public BncGame(
            @JsonProperty("id") long id,
            @JsonProperty("creatorId") long creatorId,
            @JsonProperty("answer") String answer,
            @JsonProperty("length") int length,
            @JsonProperty("history") List<BncResult> history,
            @JsonProperty("checkedNumbers") Set<String> checkedNumbers,
            @JsonProperty("startTime") long startTime,
            @JsonProperty("hexadecimal") boolean isHexadecimal,
            @JsonProperty("attemptsLeft") int attemptsLeft
    ) {
        this.id = id;
        this.creatorId = creatorId;
        this.answer = answer;
        this.length = length;
        this.history = history;
        this.checkedNumbers = checkedNumbers;
        this.startTime = startTime;
        this.isHexadecimal = isHexadecimal;
        this.attemptsLeft = attemptsLeft;
    }

    @JsonIgnore
    public static int totalAttempts(int length, boolean isHexadecimal) {
        double k = isHexadecimal ? 3.5 : 2.5;
        return (int) (length * k);
    }

    /**
     * Check number for matching answer
     *
     * @param number number to process
     * @return results (with bulls and cows)
     * @throws GameOverException             if the given number is wrong and no attempts left for future numbers
     * @throws NumberAlreadyCheckedException if the given number is already checked
     * @throws RepeatingDigitsException      if the given number has repeating digits
     * @throws InvalidLengthException        if the number's length is different from length
     */
    @JsonIgnore
    public BncResult check(String number) {
        if (number.length() != length)
            throw new InvalidLengthException(length, number.length());
        if (hasRepeatingDigits(number))
            throw new RepeatingDigitsException(number);
        if (!isHexadecimal && !number.matches("\\d+"))
            throw new InvalidCharacterException(number);

        int bulls = 0;
        int cows = 0;
        for (int i = 0; i < number.length(); i++) {
            var ch = number.charAt(i);
            // digit on the same position
            if (ch == answer.charAt(i))
                bulls++;
                // digit on the another position (but still present)
            else if (answer.indexOf(ch) != -1)
                cows++;
        }


        if (checkedNumbers.contains(number))
            throw new NumberAlreadyCheckedException(number, new BncResult(number, bulls, cows, attemptsLeft));

        checkedNumbers.add(number);
        attemptsLeft--;
        var result = new BncResult(number, bulls, cows, attemptsLeft);
        history.add(result);

        if (!result.isWin() && attemptsLeft <= 0)
            throw new GameOverException(result, answer);
        return result;
    }

    @JsonIgnore
    public boolean hasRepeatingDigits(String number) {
        return number.chars().distinct().count() < number.length();
    }

    @JsonIgnore
    public BncGameState getGameState() {
        return new BncGameState(id, creatorId, length, Collections.unmodifiableList(history), attemptsLeft, startTime, isHexadecimal, answer);
    }

    @JsonIgnore
    private String generateAnswer(int length) {
        var list = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        if (isHexadecimal)
            list = Stream.of(list, List.of("A", "B", "C", "D", "E", "F"))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        Collections.shuffle(list);
        return String.join("", list.subList(0, length));
    }

    @JsonGetter
    public long getId() {
        return id;
    }

    @JsonGetter
    public long getCreatorId() {
        return creatorId;
    }

    @JsonGetter
    public String getAnswer() {
        return answer;
    }

    @JsonGetter
    public int getLength() {
        return length;
    }

    @JsonGetter
    public List<BncResult> getHistory() {
        return history;
    }

    @JsonGetter
    public Set<String> getCheckedNumbers() {
        return checkedNumbers;
    }

    @JsonGetter
    public long getStartTime() {
        return startTime;
    }

    @JsonGetter
    public boolean isHexadecimal() {
        return isHexadecimal;
    }

    @JsonGetter
    public int getAttemptsLeft() {
        return attemptsLeft;
    }
}
