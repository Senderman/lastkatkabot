package com.senderman.lastkatkabot.feature.weather.exception;

public class NoLocationSpecifiedException extends Exception {

    public NoLocationSpecifiedException() {
        super("No location specified for the given message context");
    }
}
