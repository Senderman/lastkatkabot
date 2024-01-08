package com.senderman.lastkatkabot.feature.tracking.service;

import com.senderman.lastkatkabot.feature.cleanup.service.DatabaseCleanupService;
import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import com.senderman.lastkatkabot.feature.tracking.repository.ChatUserRepository;
import io.micronaut.data.model.Sort;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class H2ChatUserService implements ChatUserService {

    private final ChatUserRepository repo;

    public H2ChatUserService(ChatUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public Stream<ChatUser> findAll() {
        return repo.findAll().stream();
    }

    @Override
    public long countByChatId(long chatId) {
        return repo.countByChatId(chatId);
    }

    @Override
    public void deleteByChatIdAndUserId(long chatId, long userId) {
        repo.deleteByChatIdAndUserId(chatId, userId);
    }

    @Override
    public List<ChatUser> getTwoOrLessUsersOfChat(long chatId) {
        return repo.sampleOfChat(chatId, 2);
    }

    @Override
    public List<ChatUser> findByUserId(long userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public Optional<ChatUser> findNewestUserData(long userId) {
        return repo.findFirstByUserId(userId, Sort.of(Sort.Order.desc("lastMessageDate")));
    }

    @Override
    public List<ChatUser> findByChatId(long chatId) {
        return repo.findByChatId(chatId);
    }

    @Override
    public Optional<ChatUser> findByChatIdAndUserId(long chatId, long userId) {
        return repo.findByChatIdAndUserId(chatId, userId);
    }

    @Override
    public void deleteInactiveChatUsers(long chatId) {
        repo.deleteByChatIdAndLastMessageDateLessThan(chatId, DatabaseCleanupService.inactivePeriodGeneral());
    }

    @Override
    public void delete(ChatUser chatUser) {
        repo.delete(chatUser);
    }

    @Override
    public void saveAll(Iterable<ChatUser> chatUsers) {
        // TODO split to saveAll and updateAll
        chatUsers.forEach(this::save);
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
