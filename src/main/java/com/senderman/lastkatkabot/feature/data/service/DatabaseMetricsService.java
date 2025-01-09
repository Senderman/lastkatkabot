package com.senderman.lastkatkabot.feature.data.service;

import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class DatabaseMetricsService {

    private final AtomicLong usersTotal;
    private final AtomicLong chatsTotal;
    private final AtomicLong feedbacksTotal;

    private final FeedbackService feedbackService;
    private final UserStatsService userStatsService;


    public DatabaseMetricsService(
            MeterRegistry registry,
            FeedbackService feedbackService,
            UserStatsService userStatsService
    ) {
        this.feedbackService = feedbackService;
        this.userStatsService = userStatsService;

        this.usersTotal = new AtomicLong(usersTotal());
        this.chatsTotal = new AtomicLong(chatsTotal());
        this.feedbacksTotal = new AtomicLong(feedbacksTotal());


        registry.gauge("dbmetrics.usersTotal", usersTotal);
        registry.gauge("dbmetrics.chatsTotal", chatsTotal);
        registry.gauge("dbmetrics.feedbacksTotal", feedbacksTotal);

    }

    protected long usersTotal() {
        return userStatsService.getTotalUniqueUsers();
    }

    protected long chatsTotal() {
        return userStatsService.getTotalUniqueGroups();
    }

    protected long feedbacksTotal() {
        return feedbackService.count();
    }

    @Scheduled(fixedDelay = "${bot.intervals.databaseScrape}")
    public void update() {
        usersTotal.set(usersTotal());
        chatsTotal.set(chatsTotal());
        feedbacksTotal.set(feedbacksTotal());
    }

}
