package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.ChatUser;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatUserRepository extends CrudRepository<ChatUser, String> {

    @Aggregation({
            "{ $match: { chatId: ?0 } }",
            "{ $sample: { size: ?1 } }"
    })
    List<ChatUser> sampleOfChat(long chatId, int amount);

    List<ChatUser> findByUserId(int userId);

    void deleteByChatIdAndLastMessageDateLessThan(long chatId, int lastMessageDate);

    long deleteByLastMessageDateLessThan(int lastMessageDate);

    void deleteByChatIdAndUserId(long chatId, int userId);

    boolean existsByChatIdAndUserId(long chatId, int userId);

    long countByChatId(long chatId);

}
