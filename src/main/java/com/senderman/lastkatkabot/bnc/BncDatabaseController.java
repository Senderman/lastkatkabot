package com.senderman.lastkatkabot.bnc;

import com.senderman.lastkatkabot.model.BncGameSave;
import com.senderman.lastkatkabot.repository.BncRepository;
import com.senderman.lastkatkabot.service.Serializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class BncDatabaseController {

    private final BncRepository database;
    private final Serializer serializer;
    private final Map<Long, BncGame> games;

    public BncDatabaseController(BncRepository database, Serializer serializer) {
        this.database = database;
        this.serializer = serializer;
        this.games = new HashMap<>();
        for (var gameSave : database.findAll()) {
            var id = gameSave.getId();
            var game = serializer.deserialize(gameSave.getGame(), BncGame.class);
            games.put(id, game);
        }
    }

    /**
     * Check number for matching answer
     *
     * @param id     id of the game
     * @param number number to process
     * @return results (with bulls and cows)
     * @throws GameOverException             if the given number is wrong and no attempts left for future numbers
     * @throws NumberAlreadyCheckedException if the given number is already checked
     * @throws InvalidLengthException        if the number's length is different from answer length
     * @throws NoSuchElementException        if there's no game with given id
     */
    public BncResult check(long id, String number) {
        validateExistence(id);

        var game = games.get(id);
        var result = game.check(number);
        save(game);
        return result;
    }

    public boolean createGameIfNotExists(long id, int length) {
        if (games.containsKey(id)) return false;

        var game = new BncGame(id, length);
        games.put(id, game);
        save(game);
        return true;
    }

    private void validateExistence(long id) {
        if (!games.containsKey(id))
            throw new NoSuchElementException("No game with id " + id);
    }

    public BncGameState getGameState(long id) {
        validateExistence(id);
        return games.get(id).getGameState();
    }

    public void deleteGame(long id) {
        games.remove(id);
        database.deleteById(id);
    }

    private void save(BncGame game) {
        database.save(new BncGameSave(game.getId(), serializer.serialize(game)));
    }
}
