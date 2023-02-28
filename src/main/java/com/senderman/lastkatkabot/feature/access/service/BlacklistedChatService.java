package com.senderman.lastkatkabot.feature.access.service;

import com.senderman.lastkatkabot.feature.access.model.BlacklistedChat;

import java.util.Collection;
import java.util.List;

public interface BlacklistedChatService {

    BlacklistedChat save(BlacklistedChat chat);

    void deleteById(long chatId);

    List<BlacklistedChat> findByChatIdIn(Collection<Long> ids);

    boolean existsById(long chatId);

}
