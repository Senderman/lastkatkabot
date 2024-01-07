package com.senderman.lastkatkabot.feature.bnc.service;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameMessage;
import com.senderman.lastkatkabot.feature.bnc.repository.BncGameMessageRepository;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;

@Singleton
public class H2BncGameMessageService implements BncGameMessageService {

    private final BncGameMessageRepository repo;

    public H2BncGameMessageService(BncGameMessageRepository repo) {
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
