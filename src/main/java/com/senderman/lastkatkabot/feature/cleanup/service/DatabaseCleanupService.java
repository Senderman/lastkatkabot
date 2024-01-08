package com.senderman.lastkatkabot.feature.cleanup.service;

import com.senderman.lastkatkabot.feature.cleanup.model.DbCleanupResults;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public interface DatabaseCleanupService {

    int INACTIVE_PERIOD_GENERAL_SECS = (int) TimeUnit.DAYS.toSeconds(14);
    int INACTIVE_PERIOD_CAKE_SECS = (int) TimeUnit.MINUTES.toSeconds(40);

    static int inactivePeriodGeneral() {
        return (int) (System.currentTimeMillis() / 1000 - INACTIVE_PERIOD_GENERAL_SECS);
    }

    static Timestamp inactivePeriodGeneralTs() {
        return Timestamp.valueOf(LocalDateTime.now().minusSeconds(INACTIVE_PERIOD_GENERAL_SECS));
    }

    static Timestamp inactivePeriodCake() {
        return Timestamp.valueOf(LocalDateTime.now().minusSeconds(INACTIVE_PERIOD_CAKE_SECS));
    }

    long cleanInactiveUsers();

    long cleanEmptyChats();

    long cleanOldBncGames();

    long cleanOldMarriageRequests();

    long cleanOldCakes();

    DbCleanupResults cleanAll();

}
