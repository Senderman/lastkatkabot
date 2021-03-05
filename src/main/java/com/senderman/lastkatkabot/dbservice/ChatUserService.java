package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.ChatUser;

import java.util.List;
import java.util.stream.Stream;

public interface ChatUserService {

    Stream<ChatUser> findAll();

    long countByChatId(long chatId);

    void deleteByChatIdAndUserId(long chatId, int userId);

    List<ChatUser> getTwoOrLessUsersOfChat(long chatId);

    void deleteInactiveChatUsers(long chatId);

    void delete(ChatUser chatUser);

    Iterable<ChatUser> saveAll(Iterable<ChatUser> chatUsers);

    long getTotalUsers();

    long getTotalChats();

}
