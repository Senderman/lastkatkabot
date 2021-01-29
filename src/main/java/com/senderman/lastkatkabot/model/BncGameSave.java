package com.senderman.lastkatkabot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;

import java.util.Objects;

@TypeAlias("bnc")
public class BncGameSave {
    @Id
    private long id;
    private String game;

    public BncGameSave() {
    }

    public BncGameSave(long id, String game) {
        this.id = id;
        this.game = game;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BncGameSave that = (BncGameSave) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
