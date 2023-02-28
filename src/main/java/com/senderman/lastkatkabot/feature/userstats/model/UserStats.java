package com.senderman.lastkatkabot.feature.userstats.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@MappedEntity("userstats")
public class UserStats {

    @Id
    private final long userId;
    private int duelsTotal;
    private int duelWins;
    private int bncScore;
    @Nullable
    private String cityLink; // TODO rename to City
    @Nullable
    private Long loverId;

    @Creator
    public UserStats(long userId) {
        this.userId = userId;
        this.duelsTotal = 0;
        this.duelWins = 0;
        this.bncScore = 0;
    }

    public long getUserId() {
        return userId;
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

    public @Nullable String getCityLink() {
        return cityLink;
    }

    public void setCityLink(@Nullable String cityLink) {
        this.cityLink = cityLink;
    }

    public @Nullable Long getLoverId() {
        return loverId;
    }

    public void setLoverId(@Nullable Long loverId) {
        this.loverId = loverId;
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
