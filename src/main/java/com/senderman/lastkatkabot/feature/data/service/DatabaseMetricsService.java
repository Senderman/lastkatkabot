package com.senderman.lastkatkabot.feature.data.service;

import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.tracking.service.ChatUserService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class DatabaseMetricsService {

    private final static String SCRAPE_INTERVAL = "30m";

    private final AtomicLong usersTotal;
    private final AtomicLong chatsTotal;
    private final AtomicLong feedbacksTotal;

    private final FeedbackService feedbackService;
    private final ChatUserService chatUsers;


    public DatabaseMetricsService(
            MeterRegistry registry,
            FeedbackService feedbackService, ChatUserService chatUsers
    ) {
        this.feedbackService = feedbackService;
        this.chatUsers = chatUsers;

        this.usersTotal = new AtomicLong(usersTotal());
        this.chatsTotal = new AtomicLong(chatsTotal());
        this.feedbacksTotal = new AtomicLong(feedbacksTotal());


        registry.gauge("dbmetrics.usersTotal", usersTotal);
        registry.gauge("dbmetrics.chatsTotal", chatsTotal);
        registry.gauge("dbmetrics.feedbacksTotal", feedbacksTotal);

    }

    protected long usersTotal() {
        return chatUsers.getTotalUsers();
    }

    protected long chatsTotal() {
        return chatUsers.getTotalChats();
    }

    protected long feedbacksTotal() {
        return feedbackService.count();
    }

    @Scheduled(fixedDelay = SCRAPE_INTERVAL, initialDelay = SCRAPE_INTERVAL)
    public void update() {
        usersTotal.set(usersTotal());
        chatsTotal.set(chatsTotal());
        feedbacksTotal.set(feedbacksTotal());
    }

}
