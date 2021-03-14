package com.senderman.lastkatkabot.bnc.exception;

import com.senderman.lastkatkabot.bnc.BncResult;

public class NumberAlreadyCheckedException extends RuntimeException {

    private final BncResult result;

    public NumberAlreadyCheckedException(String number, BncResult result) {
        super(number + " already checked in this game");
        this.result = result;
    }

    public BncResult getResult() {
        return result;
    }
}
