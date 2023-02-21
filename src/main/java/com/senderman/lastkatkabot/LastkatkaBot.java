package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
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
        Micronaut.run(LastkatkaBot.class, args);
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
