package com.senderman.lastkatkabot.service.weather;

import com.senderman.lastkatkabot.exception.NoSuchCityException;
import com.senderman.lastkatkabot.exception.WeatherParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class WttrWeatherService implements WeatherService {

    private static final String domain = "https://wttr.in/";
    private static final String wttrOptions = "?m0AFTq&lang=ru&format=%l\\n%t\\n%f\\n%c%C\\n%w\\n%h\\n%P";

    @Override
    public Forecast getWeatherByCity(String city) throws IOException, NoSuchCityException, WeatherParseException {
        if (!city.matches("^~?[\\p{L}\\d\\s-,.+]+"))
            throw new NoSuchCityException(city);

        var response = requestWeather(city);
        return parseResponse(response);
    }


    private Forecast parseResponse(String response) throws WeatherParseException {
        String[] content = response.split("\n");
        try {
            var title = content[0];
            var temperature = content[1];
            var feelsLike = content[2];
            var feelings = content[3].replaceAll("\\s+", ": ");
            ;
            var wind = content[4]
                    .replace("←", "⬅️")
                    .replace("→", "➡️")
                    .replace("↑", "⬆️")
                    .replace("↓", "⬇️");
            var humidity = content[5];
            var pressure = content[6];
            return new Forecast(title, temperature, feelsLike, feelings, wind, humidity, pressure);
        } catch (Exception e) {
            throw new WeatherParseException("Error while parsing content: " + response, e);
        }
    }

    private String requestWeather(String city) throws IOException, NoSuchCityException {
        var url = new URL(domain + city + wttrOptions);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        if (conn.getResponseCode() == 404)
            throw new NoSuchCityException(city);

        var out = conn.getInputStream();
        return new String(out.readAllBytes());
    }

}
