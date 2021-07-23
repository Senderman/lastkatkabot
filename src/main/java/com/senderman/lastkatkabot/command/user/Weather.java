package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.service.weather.Forecast;
import com.senderman.lastkatkabot.service.weather.NoSuchCityException;
import com.senderman.lastkatkabot.service.weather.ParseException;
import com.senderman.lastkatkabot.service.weather.WeatherService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

@Component
public class Weather implements CommandExecutor {

    private final UserStatsService userStats;
    private final WeatherService weatherService;
    private final ExecutorService threadPool;

    public Weather(UserStatsService userStats, WeatherService weatherService, ExecutorService threadPool) {
        this.userStats = userStats;
        this.weatherService = weatherService;
        this.threadPool = threadPool;
    }

    @Override
    public String command() {
        return "/weather";
    }

    @Override
    public String getDescription() {
        return "погода. Если не указать город, то покажет погоду в последнем введенном вами городе";
    }

    @Override
    public void accept(MessageContext ctx) {
        long userId = ctx.user().getId();
        ctx.setArgumentsLimit(1);
        // extract name of the city from the message
        var city = ctx.argument(0, "");
        String cityLink;

        if (city.isBlank()) {
            String dbCityLink = userStats.findById(userId).getCityLink();
            if (dbCityLink == null) {
                ctx.replyToMessage("Вы не указали город! ( /weather город ). Бот запомнит ваш выбор.")
                        .callAsync(ctx.sender);
                return;
            }
            cityLink = dbCityLink;
        } else { // if city defined in the message
            try {
                cityLink = weatherService.getCityLink(city);
                // save last defined city in db
                var user = userStats.findById(userId);
                user.setCityLink(cityLink);
                userStats.save(user);
            } catch (IOException e) {
                ctx.replyToMessage("Ошибка запроса").callAsync(ctx.sender);
                return;
            } catch (NoSuchCityException e) {
                ctx.replyToMessage("Город не найден").callAsync(ctx.sender);
                return;
            }
        }

        try {
            var text = forecastToString(weatherService.getWeatherByCityLink(cityLink));
            ctx.replyToMessage(text).callAsync(ctx.sender);
        } catch (ParseException e) {
            ctx.replyToMessage("Ошибка обработки запроса").callAsync(ctx.sender);
            throw new RuntimeException(e);
        } catch (IOException e) {
            ctx.replyToMessage("Ошибка соединения с сервисом погоды").callAsync(ctx.sender);
            throw new RuntimeException(e);
        }
    }

    private String forecastToString(Forecast forecast) {
        return "<b>" + forecast.title() + "</b>\n\n" +
               forecast.feelings() + "\n" +
               "🌡: " + forecast.temperature() + " °C\n" +
               "🤔: Ощущается как " + forecast.feelsLike() + "°C\n" +
               "💨: " + forecast.wind() + "\n" +
               "💧: " + forecast.humidity() + "\n" +
               "🧭: " + forecast.pressure();

    }


}
