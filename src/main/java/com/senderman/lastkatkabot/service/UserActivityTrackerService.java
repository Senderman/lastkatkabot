package com.senderman.lastkatkabot.service;

public interface UserActivityTrackerService {
    void updateLastMessageDate(long chatId, int userId, int messageLastDate);
}
