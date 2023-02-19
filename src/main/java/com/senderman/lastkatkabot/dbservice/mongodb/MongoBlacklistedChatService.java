package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import com.senderman.lastkatkabot.model.BlacklistedChat;
import com.senderman.lastkatkabot.repository.BlacklistedChatRepository;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;

@Singleton
public class MongoBlacklistedChatService implements BlacklistedChatService {

    private final BlacklistedChatRepository repo;

    public MongoBlacklistedChatService(BlacklistedChatRepository repo) {
        this.repo = repo;
    }

    @Override
    public BlacklistedChat save(BlacklistedChat chat) {
        return repo.save(chat);
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
