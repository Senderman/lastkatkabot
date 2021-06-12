package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BncGameMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BncGameMessageRepository extends CrudRepository<BncGameMessage, String> {

    void deleteByGameId(long gameId);

    void deleteByGameIdIn(Collection<Long> gameIds);

    List<BncGameMessage> findByGameId(long gameId);

}
