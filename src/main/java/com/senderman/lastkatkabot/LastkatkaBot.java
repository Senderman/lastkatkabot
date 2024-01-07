package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import com.senderman.lastkatkabot.handler.BotHandler;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Singleton
public class LastkatkaBot implements BotModule {

    private final BotHandler botHandler;

    public LastkatkaBot(BotHandler botHandler) {
        this.botHandler = botHandler;
    }

    public static void main(String[] args) {
        Micronaut.build(args)
                .environmentVariableIncludes(
                        "MICRONAUT_SERVER_HOST",
                        "MICRONAUT_SERVER_PORT",
                        "MICRONAUT_METRICS_ENABLED",
                        "MONGODB_URI",
                        "DB",
                        "DBUSER",
                        "DBPASS",
                        "BOT_USERNAME",
                        "BOT_TOKEN",
                        "main-admin-id",
                        "feedback-channel-id",
                        "notification-channel-id"
                )
                .classes(LastkatkaBot.class)
                .start();
    }

    @EventListener
    public void run(StartupEvent event) {
        Runner.run(List.of(this));
    }

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        return botHandler;
    }

}
