package com.senderman.lastkatkabot.service.weather;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class WttrWeatherService implements WeatherService {

    private static final String domain = "https://wttr.in/";
    private static final String wttrOptions = "?m0AFTq&lang=ru&format=%l\\n%t\\n%f\\n%c%20%C\\n%w\\n%h\\n%P";

    @Override
    public Forecast getWeatherByCity(String city) throws IOException, NoSuchCityException, ParseException {
        if (!city.matches("^~?[\\p{L}\\d\\s-,.+]+"))
            throw new NoSuchCityException(city);

        var response = makeRequest(domain + city + wttrOptions);
        if (response.responseCode == 404)
            throw new NoSuchCityException(city);

        String[] content = response.content.split("\n");
        try {
            var title = content[0];
            var temperature = content[1];
            var feelsLike = content[2];
            var feelings = content[3];
            var wind = content[4];
            var humidity = content[5];
            var pressure = content[6];
            return new Forecast(title, temperature, feelsLike, feelings, wind, humidity, pressure);
        } catch (Exception e) {
            String error = String.join("\n", content);
            throw new ParseException("Error while parsing content: " + error, e);
        }

    }

    private WttrResponse makeRequest(String link) throws IOException {
        var url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        var out = conn.getInputStream();
        return new WttrResponse(conn.getResponseCode(), new String(out.readAllBytes()));
    }

    private int checkResponse(String link) throws IOException {
        var url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return connection.getResponseCode();
    }

    private record WttrResponse(int responseCode, String content) {
    }
}
