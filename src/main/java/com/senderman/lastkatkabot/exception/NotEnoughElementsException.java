package com.senderman.lastkatkabot.exception;

public class NotEnoughElementsException extends RuntimeException {

    public NotEnoughElementsException(int needed) {
        super("Not enough elements: at least" + needed + " needed");
    }
}
