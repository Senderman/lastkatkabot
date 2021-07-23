package com.senderman.lastkatkabot.service.weather;

public class NoSuchCityException extends Exception {

    public NoSuchCityException(String city) {
        super("No city found: " + city);
    }

}
