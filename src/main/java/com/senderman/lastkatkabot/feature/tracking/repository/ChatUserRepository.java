package com.senderman.lastkatkabot.feature.tracking.repository;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface ChatUserRepository extends CrudRepository<ChatUser, ChatUser.PrimaryKey> {

    @Query("SELECT * FROM CHAT_USER WHERE CHAT_ID = :chatId ORDER BY RAND() LIMIT :amount")
    List<ChatUser> sampleOfChat(long chatId, int amount);

    List<ChatUser> findByUserId(long userId);

    Optional<ChatUser> findFirstByUserId(long userId, Sort sort);

    List<ChatUser> findByChatId(long chatId);

    Optional<ChatUser> findByChatIdAndUserId(long chatId, long userId);

    void deleteByChatIdAndLastMessageDateLessThan(long chatId, int lastMessageDate);

    long deleteByLastMessageDateLessThan(int lastMessageDate);

    void deleteByChatIdAndUserId(long chatId, long userId);

    boolean existsByChatIdAndUserId(long chatId, long userId);

    long countByChatId(long chatId);

    long countDistinctUserId();

    long countDistinctChatId();

    List<Long> findDistinctChatId();

}
