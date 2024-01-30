package com.senderman.lastkatkabot.feature.userstats.service;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import io.micronaut.core.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStatsService {

    UserStats findById(long userId);

    UserStats save(UserStats userstats);

    void saveAll(Iterable<UserStats> userstats);

    List<UserStats> findTop10BncPlayers();

    List<UserStats> findTop10BncPlayersByChat(long chatId);

    List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids);

    /**
     * Update user's name, set locale if {@link UserStats#getLocale()} returns null. Create new user if not present
     *
     * @param userId userId
     * @param name   user's firstName
     * @param locale user's languageCode
     */
    void updateOrCreateByUserId(long userId, String name, @Nullable String locale);

    List<UserStats> findByChatId(long chatId);

    List<UserStats> findRandomUsersOfChat(long chatId, int amount);

    Optional<UserStats> findByChatIdAndUserId(long chatId, long userId);

}
