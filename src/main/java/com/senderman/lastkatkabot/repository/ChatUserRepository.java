package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.ChatUser;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface ChatUserRepository extends CrudRepository<ChatUser, String> {

    Stream<ChatUser> findAllByChatId(long chatId);

    @Aggregation({
            "{ $match: { chatId: ?0 } }",
            "{ $sample: { size: ?1 } }"
    })
    List<ChatUser> sampleOfChat(long chatId, int amount);

    void deleteByChatIdAndLastMessageDateLessThan(long chatId, int lastMessageDate);

    boolean existsByChatIdAndUserId(long chatId, int userId);

    Optional<ChatUser> findByChatIdAndUserId(long chatId, int userId);

}
