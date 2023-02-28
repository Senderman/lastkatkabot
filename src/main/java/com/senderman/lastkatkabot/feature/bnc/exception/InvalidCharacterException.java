package com.senderman.lastkatkabot.feature.bnc.exception;

public class InvalidCharacterException extends RuntimeException {

    public InvalidCharacterException(String number) {
        super(number + " contains invalid character(s)!");
    }

}
