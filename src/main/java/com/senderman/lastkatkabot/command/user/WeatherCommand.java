package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.service.weather.Forecast;
import com.senderman.lastkatkabot.service.weather.NoSuchCityException;
import com.senderman.lastkatkabot.service.weather.ParseException;
import com.senderman.lastkatkabot.service.weather.WeatherService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WeatherCommand implements CommandExecutor {

    private final UserStatsService userStats;
    private final WeatherService weatherService;

    public WeatherCommand(UserStatsService userStats, WeatherService weatherService) {
        this.userStats = userStats;
        this.weatherService = weatherService;
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
    public void accept(@NotNull MessageContext ctx) {
        ctx.setArgumentsLimit(1);
        try {
            String cityLink = getCityLinkFromMessageData(ctx);
            var text = forecastToString(weatherService.getWeatherByCityLink(cityLink));
            ctx.replyToMessage(text).callAsync(ctx.sender);
            // save last defined city in db (we won't get here if exception is occurred)
            saveCityLinkToDb(cityLink, ctx.user().getId());
        } catch (NoSuchCityException e) {
            ctx.replyToMessage("Город не найден").callAsync(ctx.sender);
        } catch (NoCitySpecifiedException e) {
            ctx.replyToMessage("Вы не указали город! (/weather город). Бот запомнит ваш выбор.").callAsync(ctx.sender);
        } catch (ParseException e) {
            ctx.replyToMessage("Ошибка обработки запроса: " + e.getMessage()).callAsync(ctx.sender);
            throw new RuntimeException(e);
        } catch (IOException e) {
            ctx.replyToMessage("Ошибка соединения с сервисом погоды").callAsync(ctx.sender);
            throw new RuntimeException(e);
        }
    }

    String getCityLinkFromMessageData(MessageContext ctx) throws NoSuchCityException, IOException, NoCitySpecifiedException {
        var city = ctx.argument(0, "");
        long userId = ctx.user().getId();
        String cityLink = getCityLinkFromCityOrDb(city, userId);
        if (cityLink == null)
            throw new NoCitySpecifiedException();

        return cityLink;
    }

    @Nullable
    String getCityLinkFromCityOrDb(String city, long userId) throws NoSuchCityException, IOException {
        // if no city specified in message, return city link from DB
        if (city.isBlank())
            return userStats.findById(userId).getCityLink();
        // otherwise, get link for new city and update db
        return weatherService.getCityLink(city);
    }

    private void saveCityLinkToDb(String cityLink, long userId) {
        var user = userStats.findById(userId);
        user.setCityLink(cityLink);
        userStats.save(user);
    }

    private String forecastToString(Forecast forecast) {
        return "<b>" + forecast.title() + "</b>\n\n" +
                forecast.feelings() + "\n" +
                "🌡: " + forecast.temperature() + "\n" +
                "🤔: Ощущается как " + forecast.feelsLike() + "\n" +
                "💨: " + forecast.wind() + "\n" +
                "💧: " + forecast.humidity() + "\n" +
                "🧭: " + forecast.pressure();
    }

    private static class NoCitySpecifiedException extends Exception {

    }

}
