package com.senderman.lastkatkabot.bnc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senderman.lastkatkabot.bnc.exception.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BncGame {

    private  long id;
    private  String answer;
    private  int length;
    private  List<BncResult> history;
    private  Set<String> checkedNumbers;
    private  long startTime;
    private  boolean isHexadecimal;
    private int attemptsLeft;

    public BncGame() {
    }

    public BncGame(long id, int length, boolean isHexadecimal) {
        this.id = id;
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
     * @throws InvalidLengthException        if the number's length is different from this.length
     */
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

    public boolean hasRepeatingDigits(String number) {
        return number.chars().distinct().count() < number.length();
    }

    @JsonIgnore
    public BncGameState getGameState() {
        return new BncGameState(id, length, Collections.unmodifiableList(history), attemptsLeft, startTime, isHexadecimal);
    }

    private String generateAnswer(int length) {
        var list = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        if (isHexadecimal)
            list = Stream.of(list, List.of("A", "B", "C", "D", "E", "F"))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        Collections.shuffle(list);
        return String.join("", list.subList(0, length));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<BncResult> getHistory() {
        return history;
    }

    public void setHistory(List<BncResult> history) {
        this.history = history;
    }

    public Set<String> getCheckedNumbers() {
        return checkedNumbers;
    }

    public void setCheckedNumbers(Set<String> checkedNumbers) {
        this.checkedNumbers = checkedNumbers;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isHexadecimal() {
        return isHexadecimal;
    }

    public void setHexadecimal(boolean hexadecimal) {
        isHexadecimal = hexadecimal;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public void setAttemptsLeft(int attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }
}
