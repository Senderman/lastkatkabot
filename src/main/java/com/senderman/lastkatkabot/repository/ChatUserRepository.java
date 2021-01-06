package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.ChatUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatUserRepository extends MongoRepository<ChatUser, String> {

    List<ChatUser> findAllByChatId(long chatId);

    void deleteByChatIdAndLastMessageDateLessThan(long chatId, int lastMessageDate);

}
