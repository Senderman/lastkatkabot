package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.Userstats;

import java.util.List;

public interface UserStatsService {

    Userstats findById(long userId);

    Userstats save(Userstats userstats);

    Iterable<Userstats> saveAll(Iterable<Userstats> userstats);

    List<Userstats> findTop10BncPlayers();

    List<Userstats> findTop10BncPlayersByChat(long chatId);

}
