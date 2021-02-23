package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.model.ChatUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CachingUserActivityTrackerService implements UserActivityTrackerService {

    public final static int FLUSH_INTERVAL = 5;
    private final ChatUserService chatUserService;
    private final ScheduledExecutorService threadPool;
    private final Map<String, ChatUser> cache = new HashMap<>();
    private int avgCacheFlushingSize = -1;

    private CachingUserActivityTrackerService(ChatUserService chatUserService, ScheduledExecutorService threadPool) {
        this.chatUserService = chatUserService;
        this.threadPool = threadPool;
    }

    public static UserActivityTrackerService newInstance(
            ChatUserService chatUserService,
            ScheduledExecutorService threadPool
    ) {
        var instance = new CachingUserActivityTrackerService(chatUserService, threadPool);
        instance.runCacheListener();
        return instance;
    }

    @Override
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
        chatUserService.saveAll(data);
        cache.clear();
    }
}
