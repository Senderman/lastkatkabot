package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.util.DbCleanupResults;

import java.util.concurrent.TimeUnit;

public interface DatabaseCleanupService {

    int INACTIVE_PERIOD = (int) TimeUnit.DAYS.toSeconds(14);

    static int inactivePeriod() {
        return (int) (System.currentTimeMillis() / 1000 - INACTIVE_PERIOD);
    }

    long cleanInactiveUsers();

    long cleanEmptyChats();

    long cleanOldBncGames();

    long cleanOldMarriageRequests();

    DbCleanupResults cleanAll();

}
