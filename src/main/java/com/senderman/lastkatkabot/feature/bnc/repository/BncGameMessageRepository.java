package com.senderman.lastkatkabot.feature.bnc.repository;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameMessage;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface BncGameMessageRepository extends CrudRepository<BncGameMessage, BncGameMessage.PrimaryKey> {

    void deleteByGameId(long gameId);

    void deleteByGameIdIn(Collection<Long> gameIds);

    List<BncGameMessage> findByGameId(long gameId);

    @Query("DELETE FROM bnc_game_message WHERE game_id NOT IN (SELECT id FROM bnc_game_save)")
    void deleteOrphanMessages();

}
