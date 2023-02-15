package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BncGameMessage;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BncGameMessageRepository extends CrudRepository<BncGameMessage, String> {

    void deleteByGameId(long gameId);

    void deleteByGameIdIn(Collection<Long> gameIds);

    List<BncGameMessage> findByGameId(long gameId);

}
