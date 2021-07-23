package com.senderman.lastkatkabot.service.weather;

import java.io.IOException;

public interface WeatherService {

    /**
     * Get link to the page with city weather
     *
     * @param city city name
     * @return link to the city, starting with /
     * @throws IOException         on connection error
     * @throws NoSuchCityException if there's no city
     */
    String getCityLink(String city) throws IOException, NoSuchCityException;

    /**
     * Get weather forecast by city link
     *
     * @param cityLink link to the city, starting with / (you can get it from {@link #getCityLink(String)}
     * @return Forecast object
     * @throws IOException    on connection error
     * @throws ParseException on parse error
     */
    Forecast getWeatherByCityLink(String cityLink) throws IOException, ParseException;

}
