package com.senderman.lastkatkabot.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.Objects;

@MappedEntity("blacklistedUser")
public class BlacklistedUser implements UserIdAndName<Long> {

    @Id
    private final long userId;
    private final String name;

    @Creator
    public BlacklistedUser(@Id long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    @Id
    public Long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlacklistedUser that = (BlacklistedUser) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "BlacklistedUser{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public String getName() {
        return name;
    }
}
