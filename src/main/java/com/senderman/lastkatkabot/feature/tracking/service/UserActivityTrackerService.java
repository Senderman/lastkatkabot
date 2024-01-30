package com.senderman.lastkatkabot.feature.tracking.service;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class UserActivityTrackerService {

    public final static String FLUSH_INTERVAL = "30s";
    public final static int MAX_CACHE_SIZE = 500;
    private final static String METER_NAME = "useractivitytracker.cache";
    private final ChatUserService chatUserService;
    private final UserStatsService userStatsService;
    private final Map<ChatUser.PrimaryKey, TrackData> cache = new HashMap<>();
    private final AtomicLong cacheSize;

    public UserActivityTrackerService(ChatUserService chatUserService, UserStatsService userStatsService, MeterRegistry meterRegistry) {
        this.chatUserService = chatUserService;
        this.userStatsService = userStatsService;
        this.cacheSize = new AtomicLong(0);
        meterRegistry.gauge(METER_NAME, cacheSize);
    }

    public synchronized void updateActualUserData(
            long chatId,
            long userId,
            String name,
            @Nullable String locale,
            int lastMessageDate
    ) {
        var pk = new ChatUser.PrimaryKey(chatId, userId);
        cache.compute(pk, (k, v) -> {
            if (v == null) {
                cacheSize.incrementAndGet();
                return new TrackData(name, locale, lastMessageDate);
            }
            v.name = name;
            v.locale = locale;
            v.lastMessageDate = lastMessageDate;
            return v;
        });
        if (cache.size() >= MAX_CACHE_SIZE)
            flush();
    }

    @Scheduled(fixedDelay = FLUSH_INTERVAL)
    protected synchronized void flush() {
        if (cache.isEmpty()) return;
        cache.forEach((k, v) -> {
            // if not in PM
            if (k.getUserId() != k.getChatId())
                chatUserService.save(new ChatUser(k, v.lastMessageDate));
            userStatsService.updateOrCreateByUserId(k.getUserId(), v.name, v.locale);
        });
        cache.clear();
        cacheSize.set(0);
    }

    private static class TrackData {
        private String name;
        private String locale;
        private int lastMessageDate;

        public TrackData(String name, String locale, int lastMessageDate) {
            this.name = name;
            this.locale = locale;
            this.lastMessageDate = lastMessageDate;
        }
    }
}
