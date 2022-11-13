package com.senderman.lastkatkabot.exception;

public class WeatherParseException extends Exception {

    public WeatherParseException(String message, Throwable t) {
        super("Failed to parse weather: " + message, t);
    }
}
