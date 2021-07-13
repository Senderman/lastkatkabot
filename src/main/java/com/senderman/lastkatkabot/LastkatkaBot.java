package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import com.senderman.lastkatkabot.config.BotConfigImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LastkatkaBot implements BotModule {

    public static void main(String[] args) {
        Runner.run("", List.of(new LastkatkaBot()));
    }

    @Override
    public @NotNull com.annimon.tgbotsmodule.BotHandler botHandler(@NotNull Config config) {
        final var configLoader = new YamlConfigLoaderService();
        final var configFile = configLoader.configFile("lastkatkabot", config.getProfile());
        final var botConfig = configLoader.loadFile(configFile, BotConfigImpl.class);
        return new BotHandler(botConfig);
    }
}
