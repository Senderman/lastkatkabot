package com.senderman.lastkatkabot.service.weather;

public class NoSuchCityException extends Exception {

    private final String city;

    public NoSuchCityException(String city) {
        super("No city found: " + city);
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
