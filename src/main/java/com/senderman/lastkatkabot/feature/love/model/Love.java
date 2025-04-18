package com.senderman.lastkatkabot.feature.love.model;

import com.senderman.lastkatkabot.config.BotConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Love {

    private final Map<String, List<String>> localizedLove;
    private final BotConfig config;

    public Love(Map<String, List<String>> localizedLove, BotConfig config) {
        this.localizedLove = localizedLove;
        this.config = config;
    }

    public String[] forLocale(String locale) {
        var localizedLoveStrings = localizedLove.getOrDefault(locale, localizedLove.get(config.locale().defaultLocale()));
        var choosenVariant = localizedLoveStrings.get(ThreadLocalRandom.current().nextInt(localizedLoveStrings.size()));
        return choosenVariant.split("\n");
    }

}
