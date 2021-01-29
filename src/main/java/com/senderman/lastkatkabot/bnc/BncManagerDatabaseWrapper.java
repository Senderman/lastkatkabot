package com.senderman.lastkatkabot.bnc;

import com.senderman.lastkatkabot.model.BncGameSave;
import com.senderman.lastkatkabot.repository.BncRepository;
import com.senderman.lastkatkabot.service.Serializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("bncManagerDatabaseWrapper")
public class BncManagerDatabaseWrapper implements BncGamesManager {

    private final BncRepository database;
    private final Serializer serializer;
    private final BncGamesManager gamesManager;

    public BncManagerDatabaseWrapper(BncRepository database,
                                     Serializer serializer,
                                     @Qualifier("simpleBncGamesManager") BncGamesManager gamesManager
    ) {
        this.database = database;
        this.serializer = serializer;
        this.gamesManager = gamesManager;
        for (var gameSave : database.findAll()) {
            var game = serializer.deserialize(gameSave.getGame(), BncGame.class);
            gamesManager.addGame(game);
        }
    }


    @Override
    public BncResult check(long id, String number) {
        var result = gamesManager.check(id, number);
        var game = gamesManager.getGame(id);
        saveToDb(game);
        return result;
    }

    @Override
    public boolean createGameIfNotExists(long id, int length) {
        if (!gamesManager.createGameIfNotExists(id, length)) return false;

        var game = gamesManager.getGame(id);
        saveToDb(game);
        return true;
    }


    @Override
    @NotNull
    public BncGameState getGameState(long id) {
        return gamesManager.getGameState(id);
    }

    @Override
    public void deleteGame(long id) {
        gamesManager.deleteGame(id);
        database.deleteById(id);
    }

    @Override
    public boolean addGame(BncGame game) {
        if (!gamesManager.addGame(game)) return false;

        saveToDb(game);
        return true;
    }

    @Override
    public @NotNull BncGame getGame(long id) {
        return gamesManager.getGame(id);
    }

    private void saveToDb(BncGame game) {
        database.save(new BncGameSave(game.getId(), serializer.serialize(game)));
    }
}
