package com.senderman.lastkatkabot.feature.tracking.service;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;

import java.util.List;
import java.util.Optional;

public interface ChatUserService {

    void deleteByChatIdAndUserId(long chatId, long userId);

    List<ChatUser> findByUserId(long userId);

    Optional<ChatUser> findByChatIdAndUserId(long chatId, long userId);

    ChatUser save(ChatUser user);

    long getTotalUsers();

    long getTotalChats();

    Iterable<Long> getChatIds();

}
