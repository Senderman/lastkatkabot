package com.senderman.lastkatkabot.feature.weather.service;

import com.senderman.lastkatkabot.feature.weather.exception.NoSuchLocationException;
import com.senderman.lastkatkabot.feature.weather.exception.WeatherParseException;
import com.senderman.lastkatkabot.feature.weather.model.Forecast;

import java.io.IOException;

public interface WeatherService {

    /**
     * Get weather forecast by location name
     *
     * @param location   name of location
     * @param locale desired locale of the response
     * @return Forecast object
     * @throws IOException           on connection error
     * @throws NoSuchLocationException   if there's no such location
     * @throws WeatherParseException on parse error
     */
    Forecast getWeatherByLocation(String location, String locale) throws IOException, NoSuchLocationException, WeatherParseException;

}
