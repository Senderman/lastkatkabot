package com.senderman.lastkatkabot.feature.cleanup.service;

import com.senderman.lastkatkabot.feature.cleanup.model.DbCleanupResults;

import java.util.concurrent.TimeUnit;

public interface DatabaseCleanupService {

    int INACTIVE_PERIOD_GENERAL = (int) TimeUnit.DAYS.toSeconds(14);
    int INACTIVE_PERIOD_CAKE = (int) TimeUnit.MINUTES.toSeconds(40);

    static int inactivePeriodGeneral() {
        return (int) (System.currentTimeMillis() / 1000 - INACTIVE_PERIOD_GENERAL);
    }

    static int inactivePeriodCake() {
        return (int) (System.currentTimeMillis() / 1000 - INACTIVE_PERIOD_CAKE);
    }

    long cleanInactiveUsers();

    long cleanEmptyChats();

    long cleanOldBncGames();

    long cleanOldMarriageRequests();

    long cleanOldCakes();

    DbCleanupResults cleanAll();

}
