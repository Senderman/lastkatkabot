package com.senderman.lastkatkabot.feature.weather.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.feature.weather.exception.NoSuchLocationException;
import com.senderman.lastkatkabot.feature.weather.exception.WeatherParseException;
import com.senderman.lastkatkabot.feature.weather.model.Forecast;
import com.senderman.lastkatkabot.feature.weather.service.WeatherService;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

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
    public void accept(@NotNull L10nMessageContext ctx) {
        final var messageToDelete = ctx.replyToMessage(ctx.getString("weather.connecting")).call(ctx.sender);

        final Runnable deleteMessageConsumer = () -> Methods.deleteMessage(
                        messageToDelete.getChatId(),
                        messageToDelete.getMessageId())
                .callAsync(ctx.sender);

        threadPool.execute(() -> {
            try {
                Methods.editMessageText(
                                messageToDelete.getChatId(),
                                messageToDelete.getMessageId(),
                                ctx.getString("weather.connecting"))
                        .call(ctx.sender);
                String location = getLocationFromMessageOrDb(ctx);
                var forecast = weatherService.getWeatherByLocation(location, ctx.getLocale());
                var text = forecastToString(forecast, ctx);

                if (forecast.image() == null) {// if there's no weather image, reply with text
                    ctx.replyToMessage(text).callAsync(ctx.sender);
                } else { // else reply with photo
                    ctx.replyWithPhoto()
                            .setFile("forecast.png", forecast.image())
                            .setCaption(text)
                            .enableHtml()
                            .callAsync(ctx.sender);
                }
                // save last defined location in db (we won't get here if exception is occurred)
                updateUserLocation(ctx.user().getId(), location);
            } catch (NoLocationSpecifiedException e) {
                ctx.replyToMessage(ctx.getString("weather.noLocationGiven")).callAsync(ctx.sender);
            } catch (NoSuchLocationException e) {
                ctx.replyToMessage(ctx.getString("weather.locationNotFound").formatted(e.getLocation())).callAsync(ctx.sender);
            } catch (WeatherParseException e) {
                ctx.replyToMessage(ctx.getString("weather.queryError").formatted(e.getMessage())).callAsync(ctx.sender);
                throw new RuntimeException(e);
            } catch (Throwable t) {
                ctx.replyToMessage(ctx.getString("weather.connectionError")).callAsync(ctx.sender);
                throw new RuntimeException(t);
            } finally {
                deleteMessageConsumer.run();
            }
        });
    }

    /**
     * Get user's location from message text. If it's empty, query db for it
     *
     * @param ctx message context
     * @return user's location
     * @throws NoLocationSpecifiedException if the location is found neither in message text, neither in db
     */
    private String getLocationFromMessageOrDb(L10nMessageContext ctx) throws NoLocationSpecifiedException {
        if (ctx.message().isReply() && ctx.message().getReplyToMessage().hasLocation()) {
            var location = ctx.message().getReplyToMessage().getLocation();
            return "%s,%s".formatted(location.getLatitude(), location.getLongitude());
        }
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() != 0)
            return ctx.argument(0);

        var location = userStats.findById(ctx.user().getId()).getLocation();
        if (location != null) return location;
        throw new NoLocationSpecifiedException();
    }

    private void updateUserLocation(long userId, String location) {
        var user = userStats.findById(userId);
        user.setLocation(location);
        userStats.save(user);
    }


    private String forecastToString(Forecast f, L10nMessageContext ctx) {
        return "<b>" + f.title() + "</b>\n\n" +
                f.feelings() + "\n" +
                "🌡: " + f.temperature() + "\n" +
                "🤔: %s ".formatted(ctx.getString("weather.feelsLike")) + f.feelsLike() + "\n" +
                "💨: " + f.wind() + "\n" +
                "💧: " + f.humidity() + "\n" +
                "🧭: " + f.pressure() + "\n" +
                "🌚: " + f.moonPhase() + "\n";
    }

    private static class NoLocationSpecifiedException extends Exception {

    }

}
