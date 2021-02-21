package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.ChatUserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserActivityTrackerService {

    public final static int FLUSH_INTERVAL = 10;
    private final ChatUserRepository chatUserRepo;
    private final ScheduledExecutorService threadPool;
    private final Map<String, ChatUser> cache = new HashMap<>();
    private int avgCacheFlushingSize = -1;

    private UserActivityTrackerService(ChatUserRepository chatUserRepo, ScheduledExecutorService threadPool) {
        this.chatUserRepo = chatUserRepo;
        this.threadPool = threadPool;
    }

    public static UserActivityTrackerService newInstance(
            ChatUserRepository chatUserRepo,
            ScheduledExecutorService threadPool
    ) {
        var instance = new UserActivityTrackerService(chatUserRepo, threadPool);
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

    public int getAvgCacheFlushingSize() {
        return avgCacheFlushingSize;
    }

    private synchronized void flush() {
        if (cache.isEmpty()) return;
        var data = cache.values();
        avgCacheFlushingSize = avgCacheFlushingSize == -1 ? data.size() : (avgCacheFlushingSize + data.size()) / 2;
        chatUserRepo.saveAll(data);
        cache.clear();
    }
}
