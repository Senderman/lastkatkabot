package com.senderman.lastkatkabot.feature.love.model;

import com.senderman.lastkatkabot.config.BotConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Love {

    private final Map<String, List<String>> localizedLove;
    private final BotConfig botConfig;

    public Love(Map<String, List<String>> localizedLove, BotConfig botConfig) {
        this.localizedLove = localizedLove;
        this.botConfig = botConfig;
    }

    public String[] forLocale(String locale) {
        var localizedLoveStrings = localizedLove.getOrDefault(locale, localizedLove.get(botConfig.getLocale().getDefaultLocale()));
        var choosenVariant = localizedLoveStrings.get(ThreadLocalRandom.current().nextInt(localizedLoveStrings.size()));
        return choosenVariant.split("\n");
    }

}
