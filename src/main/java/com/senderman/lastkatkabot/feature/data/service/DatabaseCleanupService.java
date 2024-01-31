package com.senderman.lastkatkabot.feature.data.service;


import com.senderman.lastkatkabot.handler.BotHandler;
import io.micronaut.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public abstract class DatabaseCleanupService {

    public static final int INACTIVE_PERIOD_CAKE_SECS = (int) TimeUnit.MINUTES.toSeconds(40);
    // chatuser, marriages, bnc games
    private final int INACTIVE_PERIOD_GENERAL_SECS = (int) TimeUnit.DAYS.toSeconds(14);
    // userstats and genshin
    private final long INACTIVE_PERIOD_USER_STATS = TimeUnit.DAYS.toSeconds(365);

    private final BotHandler botHandler;

    public DatabaseCleanupService(BotHandler botHandler) {
        this.botHandler = botHandler;
    }

    protected int inactivePeriodGeneral() {
        return (int) (System.currentTimeMillis() / 1000 - INACTIVE_PERIOD_GENERAL_SECS);
    }

    protected Timestamp inactivePeriodGeneralTs() {
        return Timestamp.valueOf(LocalDateTime.now().minusSeconds(INACTIVE_PERIOD_GENERAL_SECS));
    }

    protected Timestamp inactivePeriodCake() {
        return Timestamp.valueOf(LocalDateTime.now().minusSeconds(INACTIVE_PERIOD_CAKE_SECS));
    }

    protected Timestamp inactivePeriodUserStats() {
        return Timestamp.valueOf(LocalDateTime.now().minusSeconds(INACTIVE_PERIOD_USER_STATS));
    }

    public abstract void cleanInactiveChatUsers();

    public abstract void cleanEmptyChats();

    public abstract void cleanOldBncGames();

    public abstract void cleanOldMarriageRequests();

    public abstract void cleanOldCakes();

    public abstract void cleanInactiveUserStats();

    public abstract void cleanOldGenshinData();

    public abstract void defragmentFeedbackIds();

    @Scheduled(fixedDelay = "2h")
    public void cleanAll() {
        try {
            cleanInactiveChatUsers();
            cleanEmptyChats();
            cleanOldBncGames();
            cleanOldMarriageRequests();
            cleanOldCakes();
            cleanInactiveUserStats();
            cleanOldGenshinData();
            defragmentFeedbackIds();
        } catch (Throwable t) {
            botHandler.sendUpdateErrorAsFile(null, t);
        }
    }

}
