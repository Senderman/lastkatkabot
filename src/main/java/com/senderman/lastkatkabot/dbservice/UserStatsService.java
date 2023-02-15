package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.UserStats;

import java.util.List;

public interface UserStatsService {

    UserStats findById(long userId);

    UserStats save(UserStats userstats);

    Iterable<UserStats> saveAll(Iterable<UserStats> userstats);

    List<UserStats> findTop10BncPlayers();

    List<UserStats> findTop10BncPlayersByChat(long chatId);

}
