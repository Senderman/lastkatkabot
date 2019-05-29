package com.senderman.anitrackerbot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.beans.Config;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import org.jetbrains.annotations.NotNull;

public class AnitrackerBot implements BotModule {

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        final var configLoader = new YamlConfigLoaderService<BotConfig>();
        final var configfile = configLoader.configFile("botConfigs/anime", config.getProfile());
        final var botConfig = configLoader.load(configfile, BotConfig.class);
        return new AnitrackerBotHandler(botConfig);
    }
}
