package com.senderman.lastkatkabot.feature.tracking.service;

import com.mongodb.client.MongoDatabase;
import com.senderman.lastkatkabot.feature.cleanup.service.DatabaseCleanupService;
import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import com.senderman.lastkatkabot.feature.tracking.repository.ChatUserRepository;
import io.micronaut.data.model.Sort;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
public class MongoChatUserService implements ChatUserService {

    private final ChatUserRepository repository;
    private final MongoDatabase mongoDatabase;

    public MongoChatUserService(ChatUserRepository repository, MongoDatabase mongoDatabase) {
        this.repository = repository;
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public Stream<ChatUser> findAll() {
        return repository.findAll().stream();
    }

    @Override
    public long countByChatId(long chatId) {
        return repository.countByChatId(chatId);
    }

    @Override
    public void deleteByChatIdAndUserId(long chatId, long userId) {
        repository.deleteByChatIdAndUserId(chatId, userId);
    }

    @Override
    public List<ChatUser> getTwoOrLessUsersOfChat(long chatId) {
        return repository.sampleOfChat(chatId, 2);
    }

    @Override
    public List<ChatUser> findByUserId(long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Optional<ChatUser> findNewestUserData(long userId) {
        return repository.findFirstByUserId(userId, Sort.of(Sort.Order.desc("lastMessageDate")));
    }

    @Override
    public List<ChatUser> findByChatId(long chatId) {
        return repository.findByChatId(chatId);
    }

    @Override
    public Optional<ChatUser> findByChatIdAndUserId(long chatId, long userId) {
        return repository.findByChatIdAndUserId(chatId, userId);
    }

    @Override
    public void deleteInactiveChatUsers(long chatId) {
        repository.deleteByChatIdAndLastMessageDateLessThan(chatId, DatabaseCleanupService.inactivePeriodGeneral());
    }

    @Override
    public void delete(ChatUser chatUser) {
        repository.delete(chatUser);
    }

    @Override
    public Iterable<ChatUser> saveAll(Iterable<ChatUser> chatUsers) {
        return repository.updateAll(chatUsers);
    }

    @Override
    public long getTotalUsers() {
        return StreamSupport
                .stream(mongoDatabase.getCollection("chatUser").distinct("userId", Long.class).spliterator(), false)
                .count();
    }

    @Override
    public long getTotalChats() {
        return StreamSupport.stream(getChatIds().spliterator(), false).count();
    }

    @Override
    public Iterable<Long> getChatIds() {
        return mongoDatabase.getCollection("chatUser").distinct("chatId", Long.class);
    }
}
