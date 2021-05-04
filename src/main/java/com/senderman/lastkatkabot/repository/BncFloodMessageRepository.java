package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BncFloodMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BncFloodMessageRepository extends CrudRepository<BncFloodMessage, Long> {

    List<BncFloodMessage> findByGameId(long gameId);

    void deleteAllByGameId(long gameId);

}
