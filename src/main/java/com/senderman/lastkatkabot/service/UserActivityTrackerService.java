package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.model.ChatUser;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class UserActivityTrackerService {

    public final static String FLUSH_INTERVAL = "30s";
    public final static int MAX_CACHE_SIZE = 500;
    private final ChatUserService chatUserService;
    private final Map<String, ChatUser> cache = new HashMap<>();
    private int avgCacheFlushingSize = -1;

    public UserActivityTrackerService(ChatUserService chatUserService) {
        this.chatUserService = chatUserService;
    }

    public synchronized void updateLastMessageDate(long chatId, long userId, String name, int lastMessageDate) {
        String id = ChatUser.generateId(chatId, userId);
        cache.computeIfAbsent(id, k -> new ChatUser(chatId, userId, name, lastMessageDate));
        if (cache.size() >= MAX_CACHE_SIZE)
            flush();
    }

    public int getAvgCacheFlushingSize() {
        return avgCacheFlushingSize;
    }

    @Scheduled(fixedDelay = FLUSH_INTERVAL, scheduler = "taskScheduler")
    protected synchronized void flush() {
        if (cache.isEmpty()) return;
        var data = cache.values();
        avgCacheFlushingSize = avgCacheFlushingSize == -1 ? data.size() : (avgCacheFlushingSize + data.size()) / 2;
        chatUserService.saveAll(data);
        cache.clear();
    }
}
