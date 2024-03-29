package com.senderman.lastkatkabot.feature.bnc.service;

import com.senderman.lastkatkabot.feature.bnc.exception.*;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameState;
import com.senderman.lastkatkabot.feature.bnc.model.BncResult;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 * Interface for storing bnc games, providing ways to interact with them, but without direct game object access
 */
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

}
