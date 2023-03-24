package com.senderman.lastkatkabot.util;

import com.senderman.lastkatkabot.config.BotConfig;
import jakarta.inject.Singleton;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class CurrentTime {

    private final ZoneId timeZone;
    private final DateTimeFormatter dayFormat;

    public CurrentTime(BotConfig config) {
        this.timeZone = ZoneId.of(config.getTimezone());
        this.dayFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    }

    /**
     * Get current day as String in format yyyyMMdd
     *
     * @return current day as String in format yyyyMMdd
     */
    public String getCurrentDay() {
        return ZonedDateTime.now(timeZone).format(dayFormat);
    }
}
