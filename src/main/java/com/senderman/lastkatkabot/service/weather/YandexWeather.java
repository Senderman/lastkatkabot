package com.senderman.lastkatkabot.service.weather;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Service
public class YandexWeather implements WeatherService {

    private static final int TIMEOUT = 10000;
    private static final Charset utf8 = StandardCharsets.UTF_8;
    private static final String yandexUrl = "https://yandex.ru";
    private static final String yandexWeatherSearchUrl = yandexUrl + "/pogoda/search?request=";

    /**
     * Get link to the page with city weather
     *
     * @param city city name
     * @return link to the city, starting with /
     * @throws IOException         on connection error
     * @throws NoSuchCityException if there's no city
     */
    @Override
    public String getCityLink(String city) throws IOException, NoSuchCityException {
        var searchPage = Jsoup.parse(new URL(yandexWeatherSearchUrl + URLEncoder.encode(city, utf8)), TIMEOUT);
        try {
            return searchPage
                    .selectFirst("div.grid")
                    .selectFirst("li.place-list__item")
                    .selectFirst("a")
                    .attr("href");
        } catch (NullPointerException e) {
            throw new NoSuchCityException(city);
        }
    }

    /**
     * Get weather forecast by city link
     *
     * @param cityLink link to the city, starting with / (you can get it from {@link #getCityLink(String)}
     * @return Forecast object
     * @throws IOException    on connection error
     * @throws ParseException on parse error
     */
    @Override
    public Forecast getWeatherByCityLink(String cityLink) throws IOException, ParseException {
        try {
            return parseForecast(cityLink);
        } catch (NullPointerException e) {
            throw new ParseException(e);
        }
    }

    /**
     * Parse weather forecast from city's page
     *
     * @param cityLink link to city (starting with /)
     * @return Forecast object
     * @throws IOException          on connection error
     * @throws NullPointerException if jsoup fails to parse weather
     */
    private Forecast parseForecast(String cityLink) throws IOException, NullPointerException {
        var weatherPage = Jsoup.parse(new URL(yandexUrl + cityLink), 10000);
        var title = weatherPage.selectFirst("h1.header-title__title").text();
        var table = weatherPage.selectFirst("div.card_size_big");
        var temperature = table.selectFirst("div.fact__temp span.temp__value").text();
        var feelsLike = table.selectFirst("div.fact__feels-like div.term__value").text();
        var feelings = table.selectFirst("div.fact__feelings div.link__condition").text();
        var wind = table.selectFirst("div.fact__wind-speed div.term__value").text();
        var humidity = table.selectFirst("div.fact__humidity div.term__value").text();
        var pressure = table.selectFirst("div.fact__pressure div.term__value").text();
        return new Forecast(title, temperature, feelsLike, feelings, wind, humidity, pressure);
    }
}
