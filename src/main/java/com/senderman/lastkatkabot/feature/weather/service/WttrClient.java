package com.senderman.lastkatkabot.feature.weather.service;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;

import java.util.Optional;

@Client("https://wttr.in/")
public interface WttrClient {

    String SHORT_FORMAT = "%l\n%t\n%f\n%c%C\n%w\n%h\n%P\n%m";

    @Get("{location}?m0AFTMq")
    @Retryable
    Optional<String> getShortWeather(
            @PathVariable("location") String location,
            @Nullable @QueryValue(value = "format", defaultValue = SHORT_FORMAT) String format,
            @QueryValue(value = "lang", defaultValue = "${bot.locale.defaultLocale}") String lang
    );

    @Get("{location}?ATdM")
    @Retryable
    Optional<String> getFullWeatherAscii(
            @PathVariable("location") String location,
            @QueryValue(value = "lang", defaultValue = "${bot.locale.defaultLocale}") String lang
    );

}
