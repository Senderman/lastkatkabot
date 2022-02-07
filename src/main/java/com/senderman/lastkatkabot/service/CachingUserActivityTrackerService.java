package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.model.ChatUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CachingUserActivityTrackerService implements UserActivityTrackerService {

    public final static int FLUSH_INTERVAL = 30;
    private final ChatUserService chatUserService;
    private final ScheduledExecutorService threadPool;
    private final Map<String, ChatUser> cache = new HashMap<>();
    private int avgCacheFlushingSize = -1;

    public CachingUserActivityTrackerService(
            ChatUserService chatUserService,
            @Qualifier("userActivityTrackerPool") ScheduledExecutorService threadPool
    ) {
        this.chatUserService = chatUserService;
        this.threadPool = threadPool;
    }

    @Override
    public synchronized void updateLastMessageDate(long chatId, long userId, String name, int lastMessageDate) {
        String id = ChatUser.generateId(chatId, userId);
        var user = cache.computeIfAbsent(id, k -> new ChatUser(chatId, userId, name, lastMessageDate));
    }

    public void runCacheListener() {
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
