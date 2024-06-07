package com.senderman.lastkatkabot.config.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

@MappedEntity("settings")
public class Settings {

    @Id
    @MappedProperty("id")
    private final String id;

    @MappedProperty("data")
    private String data;

    @Creator
    public Settings(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
