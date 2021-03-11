package com.senderman.lastkatkabot.service;

public interface UserActivityTrackerService {
    void updateLastMessageDate(long chatId, long userId, int messageLastDate);
}
