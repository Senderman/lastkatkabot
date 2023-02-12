package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.BncGameMessageService;
import com.senderman.lastkatkabot.model.BncGameMessage;
import com.senderman.lastkatkabot.repository.BncGameMessageRepository;

import java.util.Collection;
import java.util.List;

@Singleton
public class MongoBncGameMessageService implements BncGameMessageService {

    private final BncGameMessageRepository repo;

    public MongoBncGameMessageService(BncGameMessageRepository repo) {
        this.repo = repo;
    }

    @Override
    public void deleteByGameId(long gameId) {
        repo.deleteByGameId(gameId);
    }

    @Override
    public void deleteByGameIdIn(Collection<Long> gameIds) {
        repo.deleteByGameIdIn(gameIds);
    }

    @Override
    public List<BncGameMessage> findByGameId(long gameId) {
        return repo.findByGameId(gameId);
    }

    @Override
    public BncGameMessage save(BncGameMessage gameMessage) {
        return repo.save(gameMessage);
    }
}
