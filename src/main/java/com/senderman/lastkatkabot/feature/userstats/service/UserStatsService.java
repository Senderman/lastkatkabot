package com.senderman.lastkatkabot.feature.userstats.service;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStatsService {

    /**
     * Find user stats by id. update name from name. set default locale if user not exists.
     * If user not exists, return new in-memory entity, without saving to db
     *
     * @param userId userId
     * @param name   user.first_name
     * @param locale user.language_code
     * @return {@link UserStats} entity from database, with given name,
     * or if not exists, a new one based on method's parameters
     */
    UserStats findById(long userId, String name, String locale);

    UserStats save(UserStats userstats);

    void saveAll(Iterable<UserStats> userstats);

    List<UserStats> findTop10BncPlayers();

    List<UserStats> findTop10BncPlayersByChat(long chatId);

    List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids);

    void updateOrCreateByUserId(long userId, String name, String locale);

    List<UserStats> findByChatId(long chatId);

    List<UserStats> findRandomUsersOfChat(long chatId, int amount);

    Optional<UserStats> findByChatIdAndUserId(long chatId, long userId);

}
