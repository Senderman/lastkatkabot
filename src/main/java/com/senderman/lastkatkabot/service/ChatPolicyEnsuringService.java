package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class ChatPolicyEnsuringService {

    public final static String FLUSH_INTERVAL = "30s";
    public final static int MAX_CACHE_SIZE = 500;
    private final Consumer<Long> chatPolicyViolationConsumer;
    private final BlacklistedChatService database;
    private final Set<Long> cache;

    public ChatPolicyEnsuringService(
            @Named("chatPolicyViolationConsumer") Consumer<Long> chatPolicyViolationConsumer,
            BlacklistedChatService database
    ) {
        this.chatPolicyViolationConsumer = chatPolicyViolationConsumer;
        this.database = database;
        this.cache = new HashSet<>();
    }

    public synchronized void queueViolationCheck(long chatId) {
        cache.add(chatId);
        if (cache.size() >= MAX_CACHE_SIZE)
            checkViolations();
    }

    @Scheduled(fixedDelay = FLUSH_INTERVAL, scheduler = "taskScheduler")
    private synchronized void checkViolations() {
        if (cache.isEmpty()) return;
        var violations = database.findByChatIdIn(cache);
        cache.clear();
        violations.forEach(v -> chatPolicyViolationConsumer.accept(v.getChatId()));
    }
}
