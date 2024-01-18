package com.senderman.lastkatkabot.feature.tracking.repository;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface ChatUserRepository extends CrudRepository<ChatUser, ChatUser.PrimaryKey> {

    List<ChatUser> findByUserId(long userId);

    Optional<ChatUser> findByChatIdAndUserId(long chatId, long userId);

    void deleteByLastMessageDateLessThan(int lastMessageDate);

    void deleteByChatIdAndUserId(long chatId, long userId);

    long countDistinctUserId();

    long countDistinctChatId();

    List<Long> findDistinctChatId();

}
