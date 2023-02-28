package com.senderman.lastkatkabot.feature.bnc.exception;

public class InvalidLengthException extends RuntimeException {

    public InvalidLengthException(int expected, int given) {
        super("Expected: " + expected + ", got: " + given);
    }

}
