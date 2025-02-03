package com.senderman.lastkatkabot.feature.bnc.model;


import com.senderman.lastkatkabot.feature.bnc.BncGame;
import com.senderman.lastkatkabot.util.convert.StringBncGameAttributeConverter;
import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedEntity("bnc_game_save")
public class BncGameSave {

    @Id
    @MappedProperty("id")
    private final long id;

    @MappedProperty(value = "game", converter = StringBncGameAttributeConverter.class)
    private final BncGame game;

    @MappedProperty("edit_date")
    @DateUpdated
    private LocalDateTime editDate;

    @Creator
    public BncGameSave(long id, BncGame game) {
        this.id = id;
        this.game = game;
    }

    public long getId() {
        return id;
    }

    public BncGame getGame() {
        return game;
    }

    public LocalDateTime getEditDate() {
        return editDate;
    }

    public void setEditDate(LocalDateTime editDate) {
        this.editDate = editDate;
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
