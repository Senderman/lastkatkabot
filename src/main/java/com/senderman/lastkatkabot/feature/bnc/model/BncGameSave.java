package com.senderman.lastkatkabot.feature.bnc.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.Objects;

@MappedEntity("bncGameSave")
public class BncGameSave {

    @Id
    private final long id;
    private final String game;
    private final int editDate;

    @Creator
    public BncGameSave(long id, String game, int editDate) {
        this.id = id;
        this.game = game;
        this.editDate = editDate;
    }

    public long getId() {
        return id;
    }

    public String getGame() {
        return game;
    }

    public int getEditDate() {
        return editDate;
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
