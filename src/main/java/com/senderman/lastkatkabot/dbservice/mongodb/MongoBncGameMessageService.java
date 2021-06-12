package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.BncGameMessageService;
import com.senderman.lastkatkabot.model.BncGameMessage;
import com.senderman.lastkatkabot.repository.BncGameMessageRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
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
