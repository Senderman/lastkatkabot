package com.senderman.lastkatkabot.service.weather;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class WttrWeatherService implements WeatherService {

    private static final String domain = "https://wttr.in";
    private static final String wttrOptions = "?m0AFTq&lang=ru&format=%l\\n%t\\n%f\\n%C\\n%w\\n%h\\n%P";

    @Override
    public String getCityLink(String city) throws IOException, NoSuchCityException, CountriesAreNotSupportedException {
        if (!city.matches("\\p{L}+")) {
            throw new NoSuchCityException(city);
        }
        var link = "/" + city + wttrOptions;
        if (checkResponse(domain + link) != 200)
            throw new NoSuchCityException(city);
        return link;
    }

    @Override
    public Forecast getWeatherByCityLink(String cityLink) throws IOException, ParseException {
        String[] content = getContent(domain + cityLink).split("\n");
        var title = content[0];
        var temperature = content[1];
        var feelsLike = content[2];
        var feelings = content[3];
        var wind = content[4];
        var humidity = content[5];
        var pressure = content[6];
        return new Forecast(title, temperature, feelsLike, feelings, wind, humidity, pressure);
    }

    private String getContent(String link) throws IOException {
        var url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        var out = conn.getInputStream();
        return new String(out.readAllBytes());
    }

    private int checkResponse(String link) throws IOException {
        var url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return connection.getResponseCode();
    }
}
