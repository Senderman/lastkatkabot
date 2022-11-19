package com.senderman.lastkatkabot.service.weather;

public record Forecast(
        String title,
        String temperature,
        String feelsLike,
        String feelings,
        String wind,
        String humidity,
        String pressure,
        String moonPhase
) {
}
