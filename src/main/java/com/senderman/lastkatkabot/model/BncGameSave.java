package com.senderman.lastkatkabot.model;


import java.util.Objects;

@TypeAlias("bnc")
public class BncGameSave {
    @Id
    private long id;
    private String game;
    private int editDate;


    public BncGameSave(long id, String game, int editDate) {
        this.id = id;
        this.game = game;
        this.editDate = editDate;
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

    public int getEditDate() {
        return editDate;
    }

    public void setEditDate(int editDate) {
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
