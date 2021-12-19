package com.senderman.lastkatkabot.service;

public interface UserActivityTrackerService {
    void updateLastMessageDate(long chatId, long userId, String name, int lastMessageDate);
}
