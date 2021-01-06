package com.senderman.lastkatkabot.model;

import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

import java.util.Objects;

@TypeAlias("userstats")
public class Userstats {

    @Id
    private int userId;
    private int duelsTotal;
    private int duelWins;
    private int bncScore;
    @Nullable
    private String cityLink;
    @Nullable
    private Integer loverId;

    public Userstats() {
    }

    public Userstats(int userId) {
        this.userId = userId;
        this.duelsTotal = 0;
        this.duelWins = 0;
        this.bncScore = 0;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public @Nullable Integer getLoverId() {
        return loverId;
    }

    public void setLoverId(@Nullable Integer loverId) {
        this.loverId = loverId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Userstats userstats = (Userstats) o;
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
