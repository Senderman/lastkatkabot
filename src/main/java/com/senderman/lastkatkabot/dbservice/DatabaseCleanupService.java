package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.util.DbCleanupResults;

public interface DatabaseCleanupService {

    long cleanInactiveUsers();

    long cleanEmptyChats();

    long cleanOldBncGames();

    long cleanOldMarriageRequests();

    DbCleanupResults cleanAll();

}
