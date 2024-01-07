package com.senderman.lastkatkabot.config.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("settings")
public class Settings {

    @Id
    private final String id;
    private String value;

    @Creator
    public Settings(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
