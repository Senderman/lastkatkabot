package com.senderman.lastkatkabot.feature.tracking.service;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ChatUserService {

    Stream<ChatUser> findAll();

    long countByChatId(long chatId);

    void deleteByChatIdAndUserId(long chatId, long userId);

    List<ChatUser> getTwoOrLessUsersOfChat(long chatId);

    List<ChatUser> findByUserId(long userId);

    Optional<ChatUser> findNewestUserData(long userId);

    List<ChatUser> findByChatId(long chatId);

    Optional<ChatUser> findByChatIdAndUserId(long chatId, long userId);

    void deleteInactiveChatUsers(long chatId);

    void delete(ChatUser chatUser);

    void saveAll(Iterable<ChatUser> chatUsers);

    ChatUser save(ChatUser user);

    long getTotalUsers();

    long getTotalChats();

    Iterable<Long> getChatIds();

}
