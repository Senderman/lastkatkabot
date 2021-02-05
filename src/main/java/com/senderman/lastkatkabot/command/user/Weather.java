package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class Weather implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final UserStatsRepository userStats;

    public Weather(CommonAbsSender telegram, UserStatsRepository userStats) {
        this.telegram = telegram;
        this.userStats = userStats;
    }

    @Override
    public String getTrigger() {
        return "/weather";
    }

    @Override
    public String getDescription() {
        return "погода. Если не указать город, то покажет погоду в последнем введенном вами городе";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        // extract name of the city from the message
        var city = message
                .getText()
                .strip()
                .replaceAll("/weather(?:@[a-zA-Z0-9_-]*)?\\s*", "");
        String cityLink;

        if (city.isBlank()) {
            String dbCityLink = userStats.findById(userId)
                    .map(Userstats::getCityLink)
                    .orElse(null);
            if (dbCityLink == null) {
                Methods.sendMessage(chatId, "Вы не указали город! ( /weather город ). Бот запомнит ваш выбор.")
                        .callAsync(telegram);
                return;
            }
            cityLink = dbCityLink;
        } else { // if city defined in the message
            try {
                cityLink = getCityPageLink(city);
                // save last defined city in db
                var user = userStats.findById(userId).orElseGet(() ->new Userstats(userId));
                user.setCityLink(cityLink);
                userStats.save(user);
            } catch (IOException e) {
                ApiRequests.answerMessage(message, "Ошибка запроса").callAsync(telegram);
                return;
            } catch (NullPointerException e) {
                ApiRequests.answerMessage(message, "Город не найден").callAsync(telegram);
                return;
            }
        }

        try {
            var text = parseForecast(cityLink).toString();
            ApiRequests.answerMessage(message, text).callAsync(telegram);
        } catch (Exception e) {
            ApiRequests.answerMessage(message, "Внутренняя ошибка").callAsync(telegram);
        }
    }

    private String getCityPageLink(String city) throws IOException {
        var searchPage = Jsoup.parse(
                new URL("https://yandex.ru/pogoda/search?request=" + URLEncoder.encode(city, StandardCharsets.UTF_8)),
                10000
        );
        return searchPage
                .selectFirst("div.grid")
                .selectFirst("li.place-list__item")
                .selectFirst("a").attr("href");
    }

    private Forecast parseForecast(String cityLink) throws IOException {
        var weatherPage = Jsoup.parse(new URL("https://yandex.ru" + cityLink), 10000);
        var title = weatherPage.selectFirst("h1.header-title__title").text();
        var table = weatherPage.selectFirst("div.card_size_big");
        var temperature = table.selectFirst("div.fact__temp span.temp__value").text();
        var feelsLike = table.selectFirst("div.fact__feels-like div.term__value").text();
        var feelings = table.selectFirst("div.fact__feelings div.link__condition").text();
        var wind = table.selectFirst("div.fact__wind-speed div.term__value").text();
        var humidity = table.selectFirst("div.fact__humidity div.term__value").text();
        var pressure = table.selectFirst("div.fact__pressure div.term__value").text();
        return new Forecast(title, temperature, feelsLike, feelings, wind, humidity, pressure);
    }

    private static class Forecast {
        private final String title;
        private final String temperature;
        private final String feelsLike;
        private final String feelings;
        private final String wind;
        private final String humidity;
        private final String pressure;

        public Forecast(String title, String temperature, String feelsLike,
                        String feelings, String wind, String humidity, String pressure) {
            this.title = title;
            this.temperature = temperature;
            this.feelsLike = feelsLike;
            this.feelings = feelings;
            this.wind = wind;
            this.humidity = humidity;
            this.pressure = pressure;
        }

        @Override
        public String toString() {
            return "<b>" + title + "</b>\n\n" +
                    feelings + "\n" +
                    "\uD83C\uDF21: " + temperature + " °C\n" +
                    "\uD83E\uDD14: Ощущается как " + feelsLike + "\n" +
                    "\uD83D\uDCA8: " + wind + "\n" +
                    "\uD83D\uDCA7: " + humidity + "\n" +
                    "\uD83E\uDDED: " + pressure;
        }
    }
}
