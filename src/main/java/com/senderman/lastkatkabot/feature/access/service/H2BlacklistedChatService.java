package com.senderman.lastkatkabot.feature.access.service;

import com.senderman.lastkatkabot.feature.access.model.BlacklistedChat;
import com.senderman.lastkatkabot.feature.access.repository.BlacklistedChatRepository;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;

@Singleton
public class H2BlacklistedChatService implements BlacklistedChatService {

    private final BlacklistedChatRepository repo;

    public H2BlacklistedChatService(BlacklistedChatRepository repo) {
        this.repo = repo;
    }

    @Override
    public BlacklistedChat save(BlacklistedChat chat) {
        return repo.existsById(chat.getChatId()) ? repo.update(chat) : repo.save(chat);
    }

    @Override
    public void deleteById(long chatId) {
        repo.deleteById(chatId);
    }

    @Override
    public List<BlacklistedChat> findByChatIdIn(Collection<Long> ids) {
        return repo.findByChatIdIn(ids);
    }

    @Override
    public boolean existsById(long chatId) {
        return repo.existsById(chatId);
    }
}
