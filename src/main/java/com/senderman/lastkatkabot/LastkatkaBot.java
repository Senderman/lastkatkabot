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
                        "DBHOST",
                        "DBUSER",
                        "DBPASS",
                        "BOT_USERNAME",
                        "BOT_TOKEN",
                        "MAIN_ADMIN_ID",
                        "NOTIFICATION_CHANNEL_ID"
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
