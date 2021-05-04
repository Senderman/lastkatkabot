package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.BncFloodMessage;

import java.util.List;

public interface BncFloodMessageService {

    List<BncFloodMessage> findByGameId(long gameId);

    void deleteByGameId(long gameId);

    BncFloodMessage save(BncFloodMessage bncFloodMessage);

}
