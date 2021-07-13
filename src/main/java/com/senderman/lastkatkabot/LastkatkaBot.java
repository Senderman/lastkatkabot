package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import com.google.inject.Guice;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LastkatkaBot implements BotModule {

    public static void main(String[] args) {
        Runner.run("", List.of(new LastkatkaBot()));
    }

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        var injector = Guice.createInjector(new InjectionConfig(config));
        return injector.getInstance(BotHandler.class);
    }
}
