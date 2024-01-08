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
    public void accept(@NotNull L10nMessageContext ctx) {
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
                String location = getLocationFromMessageOrDb(ctx);
                var text = forecastToString(weatherService.getWeatherByLocation(location, ctx.getLocale()), ctx);
                // send result as new message to notify user
                var newMessage = ctx.replyToMessage(text).call(ctx.sender);
                if (newMessage == null)
                    return;
                // since there's a method preprocessor that disables webPagePreview on SendMessage method,
                // we use EditMessage to re-enable it
                Methods
                        .editMessageText(newMessage.getChatId(), newMessage.getMessageId(), text).enableWebPagePreview()
                        .callAsync(ctx.sender);
                // delete previous "connecting" message
                Methods.deleteMessage(messageToEdit.getChatId(), messageToEdit.getMessageId()).callAsync(ctx.sender);
                // save last defined location in db (we won't get here if exception is occurred)
                updateUserLocation(ctx.user().getId(), location);
            } catch (NoLocationSpecifiedException e) {
                editMessageConsumer.accept(ctx.getString("weather.noLocationGiven"));
            } catch (NoSuchLocationException e) {
                editMessageConsumer.accept(ctx.getString("weather.locationNotFound").formatted(e.getLocation()));
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
                "🌚: " + f.moonPhase() + "\n" +
                "<a href=\"" + f.imageLink() + "\">\u200B</a>";
    }

    private static class NoLocationSpecifiedException extends Exception {

    }

}
