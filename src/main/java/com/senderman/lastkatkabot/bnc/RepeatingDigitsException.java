package com.senderman.lastkatkabot.bnc;

public class RepeatingDigitsException extends RuntimeException {

    public RepeatingDigitsException(String number) {
        super(number + " has repeating digits!");
    }

}
