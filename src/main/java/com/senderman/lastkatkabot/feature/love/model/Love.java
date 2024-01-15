package com.senderman.lastkatkabot.feature.love.model;

import com.senderman.lastkatkabot.feature.l10n.service.L10nService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Love {

    private final Map<String, List<String>> localizedLove;

    public Love(Map<String, List<String>> localizedLove) {
        this.localizedLove = localizedLove;
    }

    public String[] forLocale(String locale) {
        var localizedLoveStrings = localizedLove.getOrDefault(locale, localizedLove.get(L10nService.DEFAULT_LOCALE));
        var choosenVariant = localizedLoveStrings.get(ThreadLocalRandom.current().nextInt(localizedLoveStrings.size()));
        return choosenVariant.split("\n");
    }

}
