package com.senderman.lastkatkabot.feature.weather.exception;

public class NoSuchLocationException extends Exception {

    private final String location;

    public NoSuchLocationException(String location) {
        super("No location found: " + location);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }
}
