package com.senderman.lastkatkabot.feature.userstats.service;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;

import java.util.Collection;
import java.util.List;

public interface UserStatsService {

    UserStats findById(long userId);

    UserStats save(UserStats userstats);

    Iterable<UserStats> saveAll(Iterable<UserStats> userstats);

    List<UserStats> findTop10BncPlayers();

    List<UserStats> findTop10BncPlayersByChat(long chatId);

    List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids);

}
