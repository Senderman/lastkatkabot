package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.ChatUser;

import java.util.List;
import java.util.stream.Stream;

public interface ChatUserService {

    Stream<ChatUser> findAll();

    long countByChatId(long chatId);

    void deleteByChatIdAndUserId(long chatId, long userId);

    List<ChatUser> getTwoOrLessUsersOfChat(long chatId);

    List<ChatUser> findByUserId(long userId);

    void deleteInactiveChatUsers(long chatId);

    void delete(ChatUser chatUser);

    Iterable<ChatUser> saveAll(Iterable<ChatUser> chatUsers);

    long getTotalUsers();

    long getTotalChats();

    List<Long> getChatIds();

}
