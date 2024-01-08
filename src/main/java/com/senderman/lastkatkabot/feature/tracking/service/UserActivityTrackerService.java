package com.senderman.lastkatkabot.feature.tracking.service;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class UserActivityTrackerService {

    public final static String FLUSH_INTERVAL = "30s";
    private final static String METER_NAME = "useractivitytracker.cache";
    public final static int MAX_CACHE_SIZE = 500;
    private final ChatUserService chatUserService;
    private final Map<ChatUser.PrimaryKey, ChatUser> cache = new HashMap<>();
    private final AtomicLong cacheSize;

    public UserActivityTrackerService(ChatUserService chatUserService, MeterRegistry meterRegistry) {
        this.chatUserService = chatUserService;
        this.cacheSize = new AtomicLong(0);
        meterRegistry.gauge(METER_NAME, cacheSize);
    }

    public synchronized void updateLastMessageDate(long chatId, long userId, String name, int lastMessageDate) {
        var pk = new ChatUser.PrimaryKey(chatId, userId);
        cache.compute(pk, (k, v) -> {
            if (v == null) {
                cacheSize.incrementAndGet();
                return new ChatUser(pk, name, lastMessageDate);
            }
            v.setLastMessageDate(lastMessageDate);
            return v;
        });
        if (cache.size() >= MAX_CACHE_SIZE)
            flush();
    }

    @Scheduled(fixedDelay = FLUSH_INTERVAL)
    protected synchronized void flush() {
        if (cache.isEmpty()) return;
        var data = cache.values();
        chatUserService.saveAll(data);
        cache.clear();
        cacheSize.set(0);
    }
}
