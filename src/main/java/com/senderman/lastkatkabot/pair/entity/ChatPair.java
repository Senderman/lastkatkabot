package com.senderman.lastkatkabot.pair.entity;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ChatPair {

    @JsonProperty("pair")
    private List<Integer> pair;

    public List<Integer> getPair() {
        return pair;
    }
}
