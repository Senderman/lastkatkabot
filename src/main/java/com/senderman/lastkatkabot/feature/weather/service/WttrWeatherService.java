package com.senderman.lastkatkabot.feature.weather.service;

import com.senderman.lastkatkabot.feature.media.MediaGenerationService;
import com.senderman.lastkatkabot.feature.weather.exception.NoSuchLocationException;
import com.senderman.lastkatkabot.feature.weather.exception.WeatherParseException;
import com.senderman.lastkatkabot.feature.weather.model.Forecast;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class WttrWeatherService implements WeatherService {

    private static final Pattern windPattern = Pattern.compile("(\\D+)(\\d+)\\D+");
    private final WttrClient wttrClient;
    private final MediaGenerationService mediaGenerationService;

    public WttrWeatherService(WttrClient wttrClient, MediaGenerationService mediaGenerationService) {
        this.wttrClient = wttrClient;
        this.mediaGenerationService = mediaGenerationService;
    }

    @Override
    public Forecast getWeatherByLocation(String location, String locale) throws NoSuchLocationException, WeatherParseException {
        if (!location.matches("^~?[\\p{L}\\d\\s-,.+]+"))
            throw new NoSuchLocationException(location);

        var response = wttrClient.getShortWeather(location, null);
        if (response.isEmpty())
            throw new NoSuchLocationException(location);

        return parseResponse(response.get(), location, locale);
    }


    private Forecast parseResponse(String response, String location, String locale) throws WeatherParseException {
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
            var image = getFullWeatherImage(location, locale);
            return new Forecast(title, temperature, feelsLike, feelings,
                    wind, humidity, pressure, moonPhase, image);
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

    @Nullable
    private InputStream getFullWeatherImage(String location, String locale) {
        var response = wttrClient.getFullWeatherAscii(location, locale);
        if (response.isEmpty())
            return null;

        var strings = response.get().split("\n");
        int start = findFirstWeatherTableIndex(strings);
        if (start == -1)
            return null;
        int end = findLastWeatherTableIndex(strings, start);
        if (end == -1)
            return null;
        String[] table = Arrays.copyOfRange(strings, start, end);
        try {
            return mediaGenerationService.generateWeatherImage(table);
        } catch (IOException e) {
            return null;
        }
    }

    private int findFirstWeatherTableIndex(String[] input) {
        for (int i = 0; i < input.length; i++) {
            if (input[i].contains("┌─────"))
                return i;
        }
        return -1;
    }

    private int findLastWeatherTableIndex(String[] input, int start) {
        for (int i = start + 1; i < input.length; i++) {
            if (input[i].contains("@igor_chubin"))
                return i;
        }
        return -1;
    }

}
