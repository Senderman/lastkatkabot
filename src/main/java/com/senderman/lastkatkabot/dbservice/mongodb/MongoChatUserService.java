package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.DatabaseCleanupService;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class MongoChatUserService implements ChatUserService {

    private final ChatUserRepository repository;
    private final MongoTemplate mongoTemplate;

    public MongoChatUserService(ChatUserRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Stream<ChatUser> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false);
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
    public void deleteInactiveChatUsers(long chatId) {
        repository.deleteByChatIdAndLastMessageDateLessThan(chatId, DatabaseCleanupService.inactivePeriod());
    }

    @Override
    public void delete(ChatUser chatUser) {
        repository.delete(chatUser);
    }

    @Override
    public Iterable<ChatUser> saveAll(Iterable<ChatUser> chatUsers) {
        return repository.saveAll(chatUsers);
    }

    @Override
    public long getTotalUsers() {
        return mongoTemplate.findDistinct("userId", ChatUser.class, Integer.class).size();
    }

    @Override
    public long getTotalChats() {
        return mongoTemplate.findDistinct("chatId", ChatUser.class, Long.class).size();
    }
}
