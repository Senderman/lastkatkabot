package com.senderman.lastkatkabot.feature.chatsettings.repository;

import com.senderman.lastkatkabot.feature.chatsettings.model.ChatInfo;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface ChatInfoRepository extends CrudRepository<ChatInfo, Long> {

    long deleteByChatIdIn(Collection<Long> chatId);

    List<Long> findDistinctchatId();
}
