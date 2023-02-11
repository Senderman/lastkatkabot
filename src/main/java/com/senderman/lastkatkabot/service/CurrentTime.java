package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.config.BotConfig;
import jakarta.inject.Singleton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@Singleton
public class CurrentTime {

    private final TimeZone timeZone;
    private final SimpleDateFormat dayFormat;

    public CurrentTime(BotConfig config) {
        this.timeZone = TimeZone.getTimeZone(config.timezone());
        this.dayFormat = new SimpleDateFormat("yyyyMMdd");
        dayFormat.setTimeZone(timeZone);
    }

    /**
     * Get current day as String in format yyyyMMdd
     *
     * @return current day as String in format yyyyMMdd
     */
    public String getCurrentDay() {
        var date = Calendar.getInstance(timeZone).getTime();
        return dayFormat.format(date);
    }
}
