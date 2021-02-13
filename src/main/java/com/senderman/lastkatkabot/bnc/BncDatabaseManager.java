package com.senderman.lastkatkabot.bnc;

import com.senderman.lastkatkabot.model.BncGameSave;
import com.senderman.lastkatkabot.repository.BncRepository;
import com.senderman.lastkatkabot.service.Serializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component("bncDatabaseManager")
public class BncDatabaseManager implements BncGamesManager {

    private final BncRepository database;
    private final Serializer serializer;

    public BncDatabaseManager(
            BncRepository database,
            Serializer serializer
    ) {
        this.database = database;
        this.serializer = serializer;
    }


    @Override
    public BncResult check(long id, String number) {
        var gameSave = database.findById(id);
        if (gameSave.isEmpty()) throw new NoSuchElementException();
        var game = deserialize(gameSave.get());
        var result = game.check(number);
        saveToDb(game);
        return result;
    }

    @Override
    public boolean createGameIfNotExists(long id, int length, boolean isHexadecimal) {
        if (database.existsById(id)) return false;

        var game = new BncGame(id, length, isHexadecimal);
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
        return new BncGameSave(game.getId(), serializer.serialize(game), (int) (System.currentTimeMillis() / 1000));
    }

    private BncGame deserialize(BncGameSave save) {
        return serializer.deserialize(save.getGame(), BncGame.class);
    }
}
