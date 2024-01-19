package com.senderman.lastkatkabot.util;

import com.senderman.lastkatkabot.config.BotConfig;
import jakarta.inject.Singleton;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
public class TimeUtils {

    private final ZoneId timeZone;
    private final DateTimeFormatter dayFormat;

    public TimeUtils(BotConfig config) {
        this.timeZone = ZoneId.of(config.getTimezone());
        this.dayFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    }

    /**
     * Get current day as int in format yyyyMMdd
     *
     * @return current day as int in format yyyyMMdd
     */
    public int getCurrentDay() {
        return Integer.parseInt(ZonedDateTime.now(timeZone).format(dayFormat));
    }

    public String formatTimeSpent(long timeSpent) {
        var sec = timeSpent;
        var mins = sec / 60;
        sec -= mins * 60;
        var hours = mins / 60;
        mins -= hours * 60;
        return "%02d:%02d:%02d".formatted(hours, mins, sec);
    }
}
