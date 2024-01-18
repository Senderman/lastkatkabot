package com.senderman.lastkatkabot.feature.chatsettings.repository;

import com.senderman.lastkatkabot.feature.chatsettings.model.ChatInfo;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.H2)
public interface ChatInfoRepository extends CrudRepository<ChatInfo, Long> {

    @Query("DELETE FROM CHAT_INFO WHERE chat_id NOT IN (SELECT DISTINCT chat_id FROM CHAT_USER)")
    void deleteEmptyChats();

}
