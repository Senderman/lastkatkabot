package com.senderman.lastkatkabot.pair.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserInChat {

    @JsonProperty("result")
    private boolean result;

    public boolean getResult() {
        return result;
    }
}
