package com.senderman.lastkatkabot.feature.access.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.util.Objects;

@MappedEntity("ADMIN_USER")
public class AdminUser implements UserIdAndName<Long> {

    @Id
    @MappedProperty("user_id")
    private final Long userId;

    @MappedProperty("name")
    private String name;

    @Creator
    public AdminUser(@Id Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminUser adminUser = (AdminUser) o;
        return Objects.equals(userId, adminUser.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "AdminUser{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
