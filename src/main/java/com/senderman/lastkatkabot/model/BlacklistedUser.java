package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class BlacklistedUser {

    @Id
    private int userId;

    public BlacklistedUser() {

    }

    public BlacklistedUser(int userId) {
        this.userId = userId;
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
                '}';
    }
}
