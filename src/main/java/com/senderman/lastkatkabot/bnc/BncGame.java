package com.senderman.lastkatkabot.bnc;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BncGame {

    private final long id;
    private final String answer;
    private final int length;
    private final List<BncResult> history;
    private final Set<String> checkedNumbers;
    private int attemptsLeft;
    private final long startTime;
    private final boolean isHexadecimal;

    public BncGame(long id, int length, boolean isHexadecimal) {
        this.id = id;
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

        if (attemptsLeft <= 0)
            throw new GameOverException(result, answer);
        return result;
    }

    public boolean hasRepeatingDigits(String number) {
        return number.chars().distinct().count() < number.length();
    }

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
}
