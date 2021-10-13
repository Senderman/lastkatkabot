package com.senderman.lastkatkabot.bnc;

import com.senderman.lastkatkabot.bnc.exception.*;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public interface BncGamesManager {

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
     * @throws RepeatingDigitsException      if the given number has repeating digits
     * @throws InvalidCharacterException     if the given number contains invalid characters
     */
    BncResult check(long id, String number);

    /**
     * Creates new game with given id and answer's length. If there's already game with given id, do nothing
     *
     * @param id        id of the game
     * @param creatorId id of the creator (e.g. tg userId)
     * @param length    length of the answer
     * @return true if the game was created, else false
     */
    boolean createGameIfNotExists(long id, long creatorId, int length, boolean isHexadecimal);

    /**
     * Return Immutable information about game
     *
     * @param id id of the game
     * @return id
     * @throws NoSuchElementException if there's no game with given id
     */
    @NotNull
    BncGameState getGameState(long id);

    /**
     * Check if the game with given id is present
     *
     * @param id id of the game
     * @return true if exists
     */
    boolean hasGame(long id);

    /**
     * Delete game with given id. If there's no game with given id, do nothing
     *
     * @param id id of the game
     */
    void deleteGame(long id);

    /**
     * Add game to manager's storage. If the games' id already in the storage, do nothing
     *
     * @param game a game to add
     * @return true if id of the game was not present, else false
     */
    boolean addGame(BncGame game);


    /**
     * Get instance of the BnC game with given id
     *
     * @param id id of the game
     * @return instance of the game
     * @throws NoSuchElementException if there's no game with given id
     */
    @NotNull
    BncGame getGame(long id);
}
