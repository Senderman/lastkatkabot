package com.senderman.lastkatkabot.feature.bnc.service;

import com.senderman.lastkatkabot.feature.bnc.BncGame;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameState;
import com.senderman.lastkatkabot.feature.bnc.model.BncResult;
import com.senderman.lastkatkabot.util.Serializer;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Singleton
public class BncDatabaseManager implements BncGamesManager {

    private final BncService database;
    private final Serializer serializer;

    public BncDatabaseManager(
            BncService database,
            Serializer serializer
    ) {
        this.database = database;
        this.serializer = serializer;
    }


    @Override
    public BncResult check(long id, String number) {
        var gameSave = database.findById(id);
        if (gameSave.isEmpty())
            throw new NoSuchElementException();
        var game = deserialize(gameSave.get());
        BncResult result;
        try {
            result = game.check(number);
        } finally { // save to db even on game over
            saveToDb(game);
        }
        return result;
    }

    @Override
    public boolean createGameIfNotExists(long id, long creatorId, int length, boolean isHexadecimal) {
        if (database.existsById(id)) return false;

        var game = new BncGame(id, creatorId, length, isHexadecimal);
        addGame(game);
        return true;
    }


    @Override
    @NotNull
    public BncGameState getGameState(long id) {
        var gameSave = database.findById(id);
        if (gameSave.isEmpty()) throw new NoSuchElementException();
        var game = deserialize(gameSave.get());
        return game.getGameState();
    }

    @Override
    public boolean hasGame(long id) {
        return database.existsById(id);
    }

    @Override
    public void deleteGame(long id) {
        database.deleteById(id);
    }

    @Override
    public boolean addGame(BncGame game) {
        if (database.existsById(game.getId())) return false;
        saveToDb(game);
        return true;
    }

    @Override
    public @NotNull BncGame getGame(long id) {
        var gameSave = database.findById(id);
        if (gameSave.isEmpty()) throw new NoSuchElementException();
        return deserialize(gameSave.get());
    }

    private void saveToDb(BncGame game) {
        database.save(serialize(game));
    }

    private BncGameSave serialize(BncGame game) {
        return new BncGameSave(game.getId(), serializer.serialize(game), Timestamp.valueOf(LocalDateTime.now()));
    }

    private BncGame deserialize(BncGameSave save) {
        return serializer.deserialize(save.getGame(), BncGame.class);
    }
}
