package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import com.senderman.lastkatkabot.handler.BotHandler;
import io.micronaut.runtime.Micronaut;
import io.micronaut.scheduling.annotation.Scheduled;
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
                        "username",
                        "token",
                        "main-admin",
                        "feedback-channel-id",
                        "notification-channel-id"
                )
                .classes(LastkatkaBot.class)
                .start();
    }

    @Scheduled(initialDelay = "1s")
    public void runBot() {
        new Thread(() -> Runner.run(List.of(this))).start();
    }

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        return botHandler;
    }

}
