package com.senderman.lastkatkabot.genshin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private Type type;
    @JsonProperty
    private int stars;
    @JsonProperty
    private String description;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getStars() {
        return stars;
    }

    public String getDescription() {
        return description;
    }

    public enum Type {
        CHARACTER, WEAPON
    }
}
