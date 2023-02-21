package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.model.ChatUser;
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
    private final Map<String, ChatUser> cache = new HashMap<>();
    private final AtomicLong cacheSize;
    private int avgCacheFlushingSize = -1;
    private final MeterRegistry meterRegistry;

    public UserActivityTrackerService(ChatUserService chatUserService, MeterRegistry meterRegistry) {
        this.chatUserService = chatUserService;
        this.meterRegistry = meterRegistry;
        this.cacheSize = new AtomicLong(0);
    }

    public synchronized void updateLastMessageDate(long chatId, long userId, String name, int lastMessageDate) {
        String id = ChatUser.generateId(chatId, userId);
        cache.compute(id, (k, v) -> {
            if (v == null) {
                cacheSize.incrementAndGet();
                return new ChatUser(chatId, userId, name, lastMessageDate);
            }
            v.setLastMessageDate(lastMessageDate);
            return v;
        });
        meterRegistry.gauge(METER_NAME, cacheSize);
        if (cache.size() >= MAX_CACHE_SIZE)
            flush();
    }

    public int getAvgCacheFlushingSize() {
        return avgCacheFlushingSize;
    }

    @Scheduled(fixedDelay = FLUSH_INTERVAL, scheduler = "taskScheduler")
    protected synchronized void flush() {
        if (cache.isEmpty()) return;
        var data = cache.values();
        avgCacheFlushingSize = avgCacheFlushingSize == -1 ? data.size() : (avgCacheFlushingSize + data.size()) / 2;
        chatUserService.saveAll(data);
        cache.clear();
        cacheSize.set(0);
        meterRegistry.gauge(METER_NAME, cacheSize);
    }
}
