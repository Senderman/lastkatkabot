package com.senderman.lastkatkabot.feature.bnc.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.sql.Timestamp;
import java.util.Objects;

@MappedEntity("BNC_GAME_SAVE")
public class BncGameSave {

    @Id
    @MappedProperty("id")
    private final long id;

    @MappedProperty("game")
    private final String game;

    @MappedProperty("edit_date")
    private final Timestamp editDate;

    @Creator
    public BncGameSave(long id, String game, Timestamp editDate) {
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

    public Timestamp getEditDate() {
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
