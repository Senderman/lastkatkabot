package com.senderman.lastkatkabot.feature.weather.service;

import com.senderman.lastkatkabot.feature.weather.exception.NoSuchCityException;
import com.senderman.lastkatkabot.feature.weather.exception.WeatherParseException;
import com.senderman.lastkatkabot.feature.weather.model.Forecast;

import java.io.IOException;

public interface WeatherService {

    /**
     * Get weather forecast by city name
     *
     * @param city name of city
     * @return Forecast object
     * @throws IOException           on connection error
     * @throws NoSuchCityException   if there's no such city
     * @throws WeatherParseException on parse error
     */
    Forecast getWeatherByCity(String city) throws IOException, NoSuchCityException, WeatherParseException;

}
