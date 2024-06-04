package com.senderman.lastkatkabot.feature.weather.model;

import java.io.InputStream;

public record Forecast(
        String title,
        String temperature,
        String feelsLike,
        String feelings,
        String wind,
        String humidity,
        String pressure,
        String moonPhase,
        InputStream image
) {
}
