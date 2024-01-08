package com.senderman.lastkatkabot.feature.bnc.service;

import com.senderman.lastkatkabot.feature.bnc.BncGame;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameState;
import com.senderman.lastkatkabot.feature.bnc.model.BncResult;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

@Singleton
public class BncDatabaseManager implements BncGamesManager {

    private final BncService database;

    public BncDatabaseManager(BncService database) {
        this.database = database;
    }

    @Override
    public BncResult check(long id, String number) {
        var game = database.findById(id)
                .map(BncGameSave::getGame)
                .orElseThrow(NoSuchElementException::new);
        try {
            return game.check(number);
        } finally { // save to db even on game over
            saveToDb(game);
        }
    }

    @Override
    public boolean createGameIfNotExists(long id, long creatorId, int length, boolean isHexadecimal) {
        if (database.existsById(id)) return false;

        var game = new BncGame(id, creatorId, length, isHexadecimal);
        saveToDb(game);
        return true;
    }

    @Override
    @NotNull
    public BncGameState getGameState(long id) {
        return database.findById(id)
                .map(g -> g.getGame().getGameState())
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public boolean hasGame(long id) {
        return database.existsById(id);
    }

    @Override
    public void deleteGame(long id) {
        database.deleteById(id);
    }

    private void saveToDb(BncGame game) {
        database.save(new BncGameSave(game.getId(), game));
    }
}
