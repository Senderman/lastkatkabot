package com.senderman.lastkatkabot.feature.bnc.repository;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameMessage;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface BncGameMessageRepository extends CrudRepository<BncGameMessage, BncGameMessage.PrimaryKey> {

    void deleteByGameId(long gameId);

    void deleteByGameIdIn(Collection<Long> gameIds);

    List<BncGameMessage> findByGameId(long gameId);

}
