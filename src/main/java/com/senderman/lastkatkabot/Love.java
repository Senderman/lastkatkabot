package com.senderman.lastkatkabot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Love {

    private List<String> loveStrings;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Love(@JsonProperty("love-strings") List<String> loveStrings) {
        this.loveStrings = loveStrings;
    }

    public String[] getRandomLoveStrings() {
        int random = ThreadLocalRandom.current().nextInt(loveStrings.size());
        return loveStrings.get(random).split("\n");
    }
}
