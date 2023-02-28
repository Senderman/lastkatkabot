package com.senderman.lastkatkabot.feature.weather.model;

public record Forecast(
        String title,
        String temperature,
        String feelsLike,
        String feelings,
        String wind,
        String humidity,
        String pressure,
        String moonPhase,
        String imageLink
) {
}
