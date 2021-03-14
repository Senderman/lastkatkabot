package com.senderman.lastkatkabot.bnc.exception;

public class InvalidCharacterException extends RuntimeException {

    public InvalidCharacterException(String number) {
        super(number + " contains invalid character(s)!");
    }

}
