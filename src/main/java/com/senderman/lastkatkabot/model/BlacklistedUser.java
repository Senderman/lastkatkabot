package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

import java.util.Objects;

@TypeAlias("blacklist")
public class BlacklistedUser implements IdAndName<Integer> {

    @Id
    private int userId;
    private String name;

    public BlacklistedUser() {

    }

    public BlacklistedUser(int userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public int getUserId() {
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
    public Integer getId() {
        return getUserId();
    }

    @Override
    public String getName() {
        return name;
    }
}
