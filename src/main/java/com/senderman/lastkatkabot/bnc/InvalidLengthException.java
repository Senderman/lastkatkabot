package com.senderman.lastkatkabot.bnc;

public class InvalidLengthException extends RuntimeException {

    public InvalidLengthException(int expected, int given) {
        super("Expected: " + expected + ", got: " + given);
    }

}
