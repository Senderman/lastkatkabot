package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.BncFloodMessageService;
import com.senderman.lastkatkabot.model.BncFloodMessage;
import com.senderman.lastkatkabot.repository.BncFloodMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoBncFloodMessageService implements BncFloodMessageService {

    private final BncFloodMessageRepository repository;

    public MongoBncFloodMessageService(BncFloodMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<BncFloodMessage> findByGameId(long id) {
        return repository.findByGameId(id);
    }

    @Override
    public void deleteByGameId(long gameId) {
        repository.deleteAllByGameId(gameId);
    }

    @Override
    public BncFloodMessage save(BncFloodMessage bncFloodMessage) {
        return repository.save(bncFloodMessage);
    }
}
