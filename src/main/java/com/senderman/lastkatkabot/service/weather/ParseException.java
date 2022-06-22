package com.senderman.lastkatkabot.service.weather;

// TODO make sane
public class ParseException extends Exception {

    public ParseException(String text) {
        super("Failed to parse weather: " + text);
    }

    public ParseException(Throwable t) {
        super(t);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
