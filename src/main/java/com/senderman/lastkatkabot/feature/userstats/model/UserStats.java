package com.senderman.lastkatkabot.feature.userstats.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.sql.Timestamp;
import java.util.Objects;

@MappedEntity("USER_STATS")
public class UserStats {

    @Id
    @MappedProperty("user_id")
    private final long userId;

    // Has to be updated every time the entity is accessed
    @MappedProperty("name")
    private String name;

    @MappedProperty("duels_total")
    private int duelsTotal;

    @MappedProperty("duel_wins")
    private int duelWins;

    @MappedProperty("bnc_score")
    private int bncScore;

    // has to be set to the one of the supported locales on entity insert, and changed only by user request
    @MappedProperty("locale")
    private String locale;

    @Nullable
    @MappedProperty("location")
    private String location;

    @Nullable
    @MappedProperty("lover_id")
    private Long loverId;

    @Nullable
    @MappedProperty("updated_at")
    @DateUpdated
    private Timestamp updatedAt;

    @Creator
    public UserStats(long userId, String name, String locale) {
        this.userId = userId;
        this.name = name;
        this.locale = locale;
        this.duelsTotal = 0;
        this.duelWins = 0;
        this.bncScore = 0;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public int getDuelsTotal() {
        return duelsTotal;
    }

    public void setDuelsTotal(int duelsTotal) {
        this.duelsTotal = duelsTotal;
    }

    public int getDuelWins() {
        return duelWins;
    }

    public void setDuelWins(int duelWins) {
        this.duelWins = duelWins;
    }

    public int getBncScore() {
        return bncScore;
    }

    public void setBncScore(int bncScore) {
        this.bncScore = bncScore;
    }

    public @Nullable String getLocation() {
        return location;
    }

    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    public @Nullable Long getLoverId() {
        return loverId;
    }

    public void setLoverId(@Nullable Long loverId) {
        this.loverId = loverId;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void increaseDuelWins() {
        this.duelWins++;
    }

    public void increaseDuelsTotal() {
        this.duelsTotal++;
    }

    public void increaseBncScore(int amount) {
        this.bncScore += amount;
    }

    public boolean hasLover() {
        return loverId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStats userstats = (UserStats) o;
        return userId == userstats.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "Userstats{" +
                "userId=" + userId +
                '}';
    }
}
