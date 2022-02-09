package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class ChatPolicyEnsuringService {

    private final Consumer<Long> chatPolicyViolationConsumer;
    private final BlacklistedChatService database;
    private final Set<Long> cache;

    public ChatPolicyEnsuringService(
            @Qualifier("chatPolicyViolationConsumer") Consumer<Long> chatPolicyViolationConsumer,
            BlacklistedChatService database
    ) {
        this.chatPolicyViolationConsumer = chatPolicyViolationConsumer;
        this.database = database;
        this.cache = new HashSet<>();
    }

    public synchronized void queueViolationCheck(long chatId) {
        cache.add(chatId);
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    private synchronized void checkViolations() {
        if (cache.isEmpty()) return;
        var violations = database.findByChatIdIn(cache);
        cache.clear();
        violations.forEach(v -> chatPolicyViolationConsumer.accept(v.getChatId()));
    }
}
