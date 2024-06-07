package com.senderman.lastkatkabot.feature.bnc.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.*;

import java.util.Objects;

@MappedEntity("bnc_record")
public class BncRecord {

    @EmbeddedId
    private final PrimaryKey primaryKey;

    private long userId;
    private String name;
    private long timeSpent;

    @Creator
    public BncRecord(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public BncRecord(int length, boolean hexadecimal) {
        this.primaryKey = new PrimaryKey(length, hexadecimal);
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    @Transient
    public int getLength() {
        return this.primaryKey.length;
    }

    @Transient
    public boolean isHexadecimal() {
        return this.primaryKey.hexadecimal;
    }

    @Embeddable
    public static class PrimaryKey {

        @MappedProperty("LENGTH")
        private final int length;

        @MappedProperty("HEXADECIMAL")
        private final boolean hexadecimal;

        public PrimaryKey(int length, boolean hexadecimal) {
            this.length = length;
            this.hexadecimal = hexadecimal;
        }

        public int getLength() {
            return length;
        }

        public boolean isHexadecimal() {
            return hexadecimal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrimaryKey that = (PrimaryKey) o;
            return length == that.length && hexadecimal == that.hexadecimal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(length, hexadecimal);
        }
    }
}
