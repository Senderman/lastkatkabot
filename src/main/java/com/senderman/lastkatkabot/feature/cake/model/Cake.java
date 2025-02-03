package com.senderman.lastkatkabot.feature.cake.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedEntity("cake")
public class Cake {

    @Nullable
    @MappedProperty("filling")
    private final String filling;

    @MappedProperty("created_at")
    @DateCreated
    private LocalDateTime createdAt;

    @Id
    @MappedProperty("id")
    private int id;

    @Creator
    public Cake(int id, @Nullable String filling) {
        this.id = id;
        this.filling = filling;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
