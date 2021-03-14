package com.senderman.lastkatkabot.bnc.exception;

public class RepeatingDigitsException extends RuntimeException {

    public RepeatingDigitsException(String number) {
        super(number + " has repeating digits!");
    }

}
