package com.senderman.lastkatkabot.bnc;

public class InvalidCharacterException extends RuntimeException {

    public InvalidCharacterException(String number) {
        super(number + " contains invalid character(s)!");
    }

}
