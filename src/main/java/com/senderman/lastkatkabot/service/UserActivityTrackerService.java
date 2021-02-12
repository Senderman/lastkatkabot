package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.ChatUserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserActivityTrackerService {

    private final static int FLUSH_INTERVAL = 30;
    private final ChatUserRepository chatUserRepo;
    private final ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ChatUser> cache = new HashMap<>();

    private UserActivityTrackerService(ChatUserRepository chatUserRepo) {
        this.chatUserRepo = chatUserRepo;
    }

    public static UserActivityTrackerService newInstance(ChatUserRepository chatUserRepo) {
        var instance = new UserActivityTrackerService(chatUserRepo);
        instance.runCacheListener();
        return instance;
    }

    public synchronized void updateLastMessageDate(long chatId, int userId, int messageLastDate) {
        String id = ChatUser.generateId(chatId, userId);
        var user = cache.computeIfAbsent(id, k -> new ChatUser(chatId, userId));
        user.setLastMessageDate(messageLastDate);
    }

    private void runCacheListener() {
        threadPool.scheduleAtFixedRate(this::flush, FLUSH_INTERVAL, FLUSH_INTERVAL, TimeUnit.SECONDS);
    }

    private synchronized void flush() {
        if (cache.isEmpty()) return;
        chatUserRepo.saveAll(cache.values());
        cache.clear();
    }
}
