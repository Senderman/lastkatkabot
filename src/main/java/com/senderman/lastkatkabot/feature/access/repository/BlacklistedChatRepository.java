package com.senderman.lastkatkabot.feature.access.repository;

import com.senderman.lastkatkabot.feature.access.model.BlacklistedChat;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface BlacklistedChatRepository extends CrudRepository<BlacklistedChat, Long> {

    List<BlacklistedChat> findByChatIdIn(Collection<Long> ids);

}
