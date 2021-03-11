package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoUserStatsService implements UserStatsService {

    private final UserStatsRepository repository;

    public MongoUserStatsService(UserStatsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Userstats findById(long userId) {
        return repository.findById(userId).orElseGet(() -> new Userstats(userId));
    }

    @Override
    public Userstats save(Userstats userstats) {
        return repository.save(userstats);
    }

    @Override
    public Iterable<Userstats> saveAll(Iterable<Userstats> userstats) {
        return repository.saveAll(userstats);
    }

    @Override
    public List<Userstats> findTop10BncPlayers() {
        return repository.findTop10ByOrderByBncScoreDesc();
    }
}
