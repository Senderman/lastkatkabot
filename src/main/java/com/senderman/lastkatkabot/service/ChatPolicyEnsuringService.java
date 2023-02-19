package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class ChatPolicyEnsuringService {

    public final static String FLUSH_INTERVAL = "30s";
    public final static int MAX_CACHE_SIZE = 500;
    private final BlacklistedChatService database;
    private final Map<Long, Consumer<Long>> cache;

    public ChatPolicyEnsuringService(BlacklistedChatService database) {
        this.database = database;
        this.cache = new HashMap<>();
    }

    public synchronized void queueViolationCheck(long chatId, Consumer<Long> onViolation) {
        cache.put(chatId, onViolation);
        if (cache.size() >= MAX_CACHE_SIZE)
            checkViolations();
    }

    @Scheduled(fixedDelay = FLUSH_INTERVAL, scheduler = "taskScheduler")
    protected synchronized void checkViolations() {
        if (cache.isEmpty()) return;
        var violations = database.findByChatIdIn(cache.keySet());
        violations.forEach(chat -> cache.getOrDefault(chat.getChatId(), (c) -> {
        }).accept(chat.getChatId()));
        cache.clear();
    }
}
