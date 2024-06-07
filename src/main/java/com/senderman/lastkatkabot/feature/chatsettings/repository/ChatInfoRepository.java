package com.senderman.lastkatkabot.feature.chatsettings.repository;

import com.senderman.lastkatkabot.feature.chatsettings.model.ChatInfo;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ChatInfoRepository extends CrudRepository<ChatInfo, Long> {

    @Query("DELETE FROM chat_info WHERE chat_id NOT IN (SELECT DISTINCT chat_id FROM chat_user)")
    void deleteEmptyChats();

}
