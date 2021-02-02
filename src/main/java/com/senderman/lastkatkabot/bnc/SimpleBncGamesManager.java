package com.senderman.lastkatkabot.bnc;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Component("simpleBncGamesManager")
public class SimpleBncGamesManager implements BncGamesManager {

    private final Map<Long, BncGame> games;

    public SimpleBncGamesManager() {
        this.games = new HashMap<>();
    }

    @Override
    public BncResult check(long id, String number) {
        validateExistence(id);
        return games.get(id).check(number);
    }

    @Override
    public boolean createGameIfNotExists(long id, int length, boolean isHexadecimal) {
        if (games.containsKey(id)) return false;

        games.put(id, new BncGame(id, length, isHexadecimal));
        return true;
    }

    @Override
    @NotNull
    public BncGameState getGameState(long id) {
        validateExistence(id);
        return games.get(id).getGameState();
    }

    private void validateExistence(long id) {
        if (!games.containsKey(id))
            throw new NoSuchElementException("No game with id " + id);
    }

    @Override
    public void deleteGame(long id) {
        games.remove(id);
    }

    @Override
    public boolean addGame(BncGame game) {
        if (games.containsKey(game.getId())) return false;
        games.put(game.getId(), game);
        return true;
    }

    @Override
    @NotNull
    public BncGame getGame(long id) {
        validateExistence(id);
        return games.get(id);
    }
}
