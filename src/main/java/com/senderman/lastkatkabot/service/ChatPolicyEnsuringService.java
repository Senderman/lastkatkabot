package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class ChatPolicyEnsuringService {

    public final static int CHECK_INTERVAL = 30;
    private final Consumer<Long> chatPolicyViolationConsumer;
    private final BlacklistedChatService database;
    private final ScheduledExecutorService threadPool;
    private final Set<Long> cache;

    public ChatPolicyEnsuringService(
            @Qualifier("chatPolicyViolationConsumer") Consumer<Long> chatPolicyViolationConsumer,
            BlacklistedChatService database,
            @Qualifier("chatPolicyPool") ScheduledExecutorService threadPool
    ) {
        this.chatPolicyViolationConsumer = chatPolicyViolationConsumer;
        this.database = database;
        this.threadPool = threadPool;
        this.cache = new HashSet<>();
    }

    public synchronized void queueViolationCheck(long chatId) {
        cache.add(chatId);
    }

    private synchronized void checkViolations() {
        if (cache.isEmpty()) return;
        var violations = database.findByChatIdIn(cache);
        cache.clear();
        violations.forEach(v -> chatPolicyViolationConsumer.accept(v.getChatId()));
    }

    public void runViolationChecker() {
        threadPool.scheduleAtFixedRate(this::checkViolations, CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
    }
}
