package com.senderman.lastkatkabot.feature.cleanup.service;

import com.senderman.lastkatkabot.feature.cleanup.model.DbCleanupResults;

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
