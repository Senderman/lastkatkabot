package com.senderman.lastkatkabot.feature.bnc.exception;

public class RepeatingDigitsException extends RuntimeException {

    public RepeatingDigitsException(String number) {
        super(number + " has repeating digits!");
    }

}
