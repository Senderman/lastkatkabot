package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Singleton
public class LastkatkaBot implements BotModule, EmbeddedApplication<LastkatkaBot> {

    private final ApplicationContext context;
    private final ApplicationConfiguration configuration;
    private final BotHandler botHandler;

    public LastkatkaBot(ApplicationContext context, ApplicationConfiguration configuration, BotHandler botHandler) {
        this.context = context;
        this.configuration = configuration;
        this.botHandler = botHandler;
    }

    public static void main(String[] args) {
        Micronaut.run(LastkatkaBot.class, args);
    }

    @Override
    public @NotNull LastkatkaBot start() {
        Runner.run(List.of(this));
        return this;
    }

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        return botHandler;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return context;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return configuration;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public boolean isServer() {
        return true;
    }

}
