package com.senderman.lastkatkabot.feature.cake.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.sql.Timestamp;
import java.util.Objects;

@MappedEntity("CAKE")
public class Cake {

    @Nullable
    @MappedProperty("filling")
    private final String filling;
    @MappedProperty("created_at")
    private final Timestamp createdAt;
    @Id
    @GeneratedValue
    @MappedProperty("id")
    private int id;

    @Creator
    public Cake(@Nullable String filling, Timestamp createdAt) {
        this.filling = filling;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public String getFilling() {
        return filling;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cake cake = (Cake) o;
        return id == cake.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
