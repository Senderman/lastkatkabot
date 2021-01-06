package com.senderman.lastkatkabot;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Love {

    @JsonProperty("love-strings")
    private List<String> loveStrings;


    public List<String> getLoveStrings() {
        return loveStrings;
    }

    public void setLoveStrings(List<String> loveStrings) {
        this.loveStrings = loveStrings;
    }

    public String[] getRandomLoveStrings() {
        int random = ThreadLocalRandom.current().nextInt(loveStrings.size());
        return loveStrings.get(random).split("\n");
    }
}
