package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

//@Singleton
public class ChatPolicyEnsuringService {

    public final static String FLUSH_INTERVAL = "30s";
    public final static int MAX_CACHE_SIZE = 500;
    private final Consumer<Long> chatPolicyViolationConsumer;
    private final BlacklistedChatService database;
    private final Set<Long> cache;

    public ChatPolicyEnsuringService(
            ApplicationContext context,
            //@Named("chatPolicyViolationConsumer") Consumer<Long> chatPolicyViolationConsumer,
            BlacklistedChatService database
    ) {
        this.chatPolicyViolationConsumer = context.getBean(Argument.of(Consumer.class, Long.class), Qualifiers.byName("chatPolicyViolationConsumer"));
        this.database = database;
        this.cache = new HashSet<>();
    }

    public synchronized void queueViolationCheck(long chatId) {
        cache.add(chatId);
        if (cache.size() >= MAX_CACHE_SIZE)
            checkViolations();
    }

    @Scheduled(fixedDelay = FLUSH_INTERVAL, scheduler = "taskScheduler")
    protected synchronized void checkViolations() {
        if (cache.isEmpty()) return;
        var violations = database.findByChatIdIn(cache);
        cache.clear();
        violations.forEach(v -> chatPolicyViolationConsumer.accept(v.getChatId()));
    }
}
