package com.senderman.lastkatkabot.feature.cleanup.service;

import com.senderman.lastkatkabot.feature.cleanup.model.DbCleanupResults;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public interface DatabaseCleanupService {

    int INACTIVE_PERIOD_GENERAL = (int) TimeUnit.DAYS.toSeconds(14);
    int INACTIVE_PERIOD_CAKE = (int) TimeUnit.MINUTES.toSeconds(40);

    // TODO remove after full db migration
    static int inactivePeriodGeneral() {
        return (int) (System.currentTimeMillis() / 1000 - INACTIVE_PERIOD_GENERAL);
    }

    static Timestamp inactivePeriodGeneralTs() {
        return Timestamp.valueOf(LocalDateTime.now().minusDays(14));
    }

    static Timestamp inactivePeriodCake() {
        return Timestamp.valueOf(LocalDateTime.now().minusMinutes(40));
    }

    long cleanInactiveUsers();

    long cleanEmptyChats();

    long cleanOldBncGames();

    long cleanOldMarriageRequests();

    long cleanOldCakes();

    DbCleanupResults cleanAll();

}
