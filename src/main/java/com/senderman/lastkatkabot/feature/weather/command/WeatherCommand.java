package com.senderman.lastkatkabot.feature.weather.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.feature.weather.exception.NoSuchCityException;
import com.senderman.lastkatkabot.feature.weather.exception.WeatherParseException;
import com.senderman.lastkatkabot.feature.weather.model.Forecast;
import com.senderman.lastkatkabot.feature.weather.service.WeatherService;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Command
public class WeatherCommand implements CommandExecutor {

    private final UserStatsService userStats;
    private final WeatherService weatherService;
    private final ExecutorService threadPool;

    public WeatherCommand(
            UserStatsService userStats,
            WeatherService weatherService,
            @Named("weatherPool") ExecutorService threadPool) {
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
        return "weather.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        final var messageToEdit = ctx.replyToMessage(ctx.getString("weather.queue")).call(ctx.sender);
        // if bot failed to send the message
        if (messageToEdit == null) {
            return;
        }
        final Consumer<String> editMessageConsumer = s -> Methods.editMessageText(
                        messageToEdit.getChatId(),
                        messageToEdit.getMessageId(),
                        s)
                .enableWebPagePreview()
                .callAsync(ctx.sender);

        threadPool.execute(() -> {
            try {
                editMessageConsumer.accept(ctx.getString("weather.connecting"));
                String city = getCityFromMessageOrDb(ctx);
                var text = forecastToString(weatherService.getWeatherByCity(city, ctx.getLocale()), ctx);
                editMessageConsumer.accept(text);
                // save last defined city in db (we won't get here if exception is occurred)
                updateUserCity(ctx.user().getId(), city);
            } catch (NoCitySpecifiedException e) {
                editMessageConsumer.accept(ctx.getString("weather.noCityGiven"));
            } catch (NoSuchCityException e) {
                editMessageConsumer.accept(ctx.getString("weather.cityNotFound").formatted(e.getCity()));
            } catch (WeatherParseException e) {
                editMessageConsumer.accept(ctx.getString("weather.queryError").formatted(e.getMessage()));
                throw new RuntimeException(e);
            } catch (IOException e) {
                editMessageConsumer.accept(ctx.getString("weather.connectionError"));
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get city from message text. If it's empty, query db for it
     *
     * @param ctx message context
     * @return user's city
     * @throws NoCitySpecifiedException if the city is found neither in message text, neither in db
     */
    private String getCityFromMessageOrDb(LocalizedMessageContext ctx) throws NoCitySpecifiedException {
        if (ctx.message().isReply() && ctx.message().getReplyToMessage().hasLocation()) {
            var location = ctx.message().getReplyToMessage().getLocation();
            return "%s,%s".formatted(location.getLatitude(), location.getLongitude());
        }
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() != 0)
            return ctx.argument(0);

        var city = userStats.findById(ctx.user().getId()).getCityLink();
        if (city != null) return city;
        throw new NoCitySpecifiedException();
    }

    private void updateUserCity(long userId, String city) {
        var user = userStats.findById(userId);
        user.setCityLink(city);
        userStats.save(user);
    }


    private String forecastToString(Forecast f, LocalizedMessageContext ctx) {
        return "<b>" + f.title() + "</b>\n\n" +
                f.feelings() + "\n" +
                "🌡: " + f.temperature() + "\n" +
                "🤔: %s ".formatted(ctx.getString("weather.feelsLike")) + f.feelsLike() + "\n" +
                "💨: " + f.wind() + "\n" +
                "💧: " + f.humidity() + "\n" +
                "🧭: " + f.pressure() + "\n" +
                "🌚: " + f.moonPhase() + "\n" +
                "<a href=\"" + f.imageLink() + "\">\u200B</a>";
    }

    private static class NoCitySpecifiedException extends Exception {

    }

}
