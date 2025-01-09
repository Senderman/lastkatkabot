package com.senderman.lastkatkabot.feature.access.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Singleton
public class ChatPolicyEnsuringService {

    private final static String METER_NAME = "chatpolicyensuring.cache";
    private final BlacklistedChatService database;
    private final Map<Long, Consumer<Long>> cache;
    private final AtomicLong cacheSize;
    private final int maxCacheSize;

    public ChatPolicyEnsuringService(
            BlacklistedChatService database,
            MeterRegistry meterRegistry,
            @Value("${bot.limits.chatPolicy}") int maxCacheSize) {
        this.database = database;
        this.maxCacheSize = maxCacheSize;
        this.cache = new HashMap<>();
        this.cacheSize = new AtomicLong(0);
        meterRegistry.gauge(METER_NAME, cacheSize);
    }

    public synchronized void queueViolationCheck(long chatId, Consumer<Long> onViolation) {
        cache.computeIfAbsent(chatId, k -> {
            cacheSize.incrementAndGet();
            return onViolation;
        });
        if (cache.size() >= maxCacheSize)
            checkViolations();
    }

    @Scheduled(fixedDelay = "${bot.intervals.chatPolicyViolationCheck}")
    protected synchronized void checkViolations() {
        if (cache.isEmpty()) return;
        var violations = database.findByChatIdIn(cache.keySet());
        violations.forEach(chat -> cache.getOrDefault(chat.getChatId(), (c) -> {
        }).accept(chat.getChatId()));
        cache.clear();
        cacheSize.set(0);
    }
}
