package com.senderman.lastkatkabot.feature.weather.service;

import com.senderman.lastkatkabot.feature.weather.exception.NoSuchCityException;
import com.senderman.lastkatkabot.feature.weather.exception.WeatherParseException;
import com.senderman.lastkatkabot.feature.weather.model.Forecast;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class WttrWeatherService implements WeatherService {

    private static final String domain = "https://wttr.in/";
    private static final String wttrOptions = "?m0AFTq&lang=ru&format=%l\\n%t\\n%f\\n%c%C\\n%w\\n%h\\n%P\\n%m";
    private static final Pattern windPattern = Pattern.compile("(\\D+)(\\d+)\\D+");

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
            var temperature = content[1].replaceAll("[+-]0", "0");
            var feelsLike = content[2].replaceAll("[+-]0", "0");
            var feelings = content[3].replaceFirst("\\s+", ": ");
            var wind = formatWind(content[4]);
            var humidity = content[5];
            var pressure = formatPressure(content[6]);
            var moonPhase = content[7];
            return new Forecast(title, temperature, feelsLike, feelings, wind, humidity, pressure, moonPhase, getImageLink(title));
        } catch (Exception e) {
            throw new WeatherParseException("Error while parsing content: " + response, e);
        }
    }

    private String formatWind(String line) {
        final Matcher m = windPattern.matcher(line);
        if (!m.find()) return normalizeWindArrows(line);

        final var windDir = normalizeWindArrows(m.group(1));
        final var windSpeed = Integer.parseInt(m.group(2));
        return "%s%d км/ч (%.0f м/с)".formatted(windDir, windSpeed, windSpeed / 3.6);
    }

    private String normalizeWindArrows(String line) {
        return line.replace("←", "⬅️")
                .replace("→", "➡️")
                .replace("↑", "⬆️")
                .replace("↓", "⬇️");
    }

    private String formatPressure(String line) {
        int hPa = Integer.parseInt(line.replaceAll("\\D*(\\d+)\\D+", "$1"));
        int mmHg = (int) (hPa * 0.7500615758456601);
        return "%d гПа (%d мм.рт.ст.)".formatted(hPa, mmHg);
    }

    private String requestWeather(String city) throws IOException, NoSuchCityException {
        var url = new URL(domain + urlEncodeCity(city) + wttrOptions);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        if (conn.getResponseCode() == 404)
            throw new NoSuchCityException(city);
        try (var out = conn.getInputStream()) {
            return new String(out.readAllBytes());
        }
    }

    private String urlEncodeCity(String city) {
        return city.replaceAll("\\s", "%20");
    }

    private String getImageLink(String city) {
        // prevent telegram caching
        long tsHours = System.currentTimeMillis() / 3600000;
        return domain + urlEncodeCity(city) + ".png?lang=ru&ts=" + tsHours;
    }

}
