package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

import java.util.Objects;

@TypeAlias("admin")
public class AdminUser {

    @Id
    private int userId;

    public AdminUser() {

    }

    public AdminUser(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
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
                '}';
    }
}
