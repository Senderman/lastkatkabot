package com.senderman.lastkatkabot.feature.tracking.service;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import com.senderman.lastkatkabot.feature.tracking.repository.ChatUserRepository;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class H2ChatUserService implements ChatUserService {

    private final ChatUserRepository repo;

    public H2ChatUserService(ChatUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void deleteByChatIdAndUserId(long chatId, long userId) {
        repo.deleteByChatIdAndUserId(chatId, userId);
    }

    @Override
    public List<ChatUser> findByUserId(long userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public Optional<ChatUser> findByChatIdAndUserId(long chatId, long userId) {
        return repo.findByChatIdAndUserId(chatId, userId);
    }

    @Override
    public ChatUser save(ChatUser user) {
        return repo.existsById(user.getPrimaryKey()) ? repo.update(user) : repo.save(user);
    }

    @Override
    public long getTotalUsers() {
        return repo.countDistinctUserId();
    }

    @Override
    public long getTotalChats() {
        return repo.countDistinctChatId();
    }

    @Override
    public Iterable<Long> getChatIds() {
        return repo.findDistinctChatId();
    }
}
