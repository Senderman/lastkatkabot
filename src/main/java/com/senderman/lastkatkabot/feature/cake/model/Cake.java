package com.senderman.lastkatkabot.feature.cake.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.Objects;

@MappedEntity("cake")
public class Cake {

    @Nullable
    private final String filling;
    private final int createdAt;
    @Id
    private int id;

    @Creator
    public Cake(int id, @Nullable String filling, int createdAt) {
        this.id = id;
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

    public int getCreatedAt() {
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
