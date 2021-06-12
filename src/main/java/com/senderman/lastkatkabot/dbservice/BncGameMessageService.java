package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.BncGameMessage;

import java.util.Collection;
import java.util.List;

public interface BncGameMessageService {

    void deleteByGameId(long gameId);

    void deleteByGameIdIn(Collection<Long> gameIds);

    List<BncGameMessage> findByGameId(long gameId);

    BncGameMessage save(BncGameMessage gameMessage);

}
