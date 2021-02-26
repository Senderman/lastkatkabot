package com.senderman.lastkatkabot.service.tracking;

public interface UserActivityTrackerService {
    void updateLastMessageDate(long chatId, int userId, int messageLastDate);
}
