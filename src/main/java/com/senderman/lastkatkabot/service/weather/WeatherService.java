package com.senderman.lastkatkabot.service.weather;

import java.io.IOException;

public interface WeatherService {

    /**
     * Get weather forecast by city name
     *
     * @param city name of city
     * @return Forecast object
     * @throws IOException         on connection error
     * @throws NoSuchCityException if there's no such city
     * @throws ParseException      on parse error
     */
    Forecast getWeatherByCity(String city) throws IOException, NoSuchCityException, ParseException;

}
