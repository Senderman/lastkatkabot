package com.senderman.lastkatkabot.model;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Objects;

public class AdminUser implements IdAndName<Long> {

    @BsonId
    private long userId;
    private String name;

    public AdminUser() {

    }

    public AdminUser(long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdminUser adminUser = (AdminUser) o;
        return userId == adminUser.userId;
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
    public Long getId() {
        return getUserId();
    }

    @Override
    public String getName() {
        return name;
    }
}
