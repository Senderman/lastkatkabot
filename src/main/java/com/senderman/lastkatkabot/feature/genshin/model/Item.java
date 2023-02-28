package com.senderman.lastkatkabot.feature.genshin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Item(
        @JsonProperty(value = "id", required = true) String id,
        @JsonProperty(value = "name", required = true) String name,
        @JsonProperty(value = "type", required = true) Type type,
        @JsonProperty(value = "stars", required = true) int stars,
        @JsonProperty(value = "description", required = true) String description
) {

    public enum Type {
        CHARACTER, WEAPON
    }
}
