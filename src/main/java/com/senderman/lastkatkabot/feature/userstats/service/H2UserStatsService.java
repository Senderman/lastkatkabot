package com.senderman.lastkatkabot.feature.userstats.service;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import com.senderman.lastkatkabot.feature.userstats.repository.UserStatsRepository;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;

@Singleton
public class H2UserStatsService implements UserStatsService {

    private final UserStatsRepository repo;

    public H2UserStatsService(UserStatsRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserStats findById(long userId) {
        return repo.findById(userId).orElseGet(() -> new UserStats(userId));
    }

    @Override
    public UserStats save(UserStats userstats) {
        return repo.existsById(userstats.getUserId()) ? repo.update(userstats) : repo.save(userstats);
    }

    @Override
    public void saveAll(Iterable<UserStats> userstats) {
        userstats.forEach(this::save);
    }

    @Override
    public List<UserStats> findTop10BncPlayers() {
        return repo.findTop10OrderByBncScoreDesc();
    }

    @Override
    public List<UserStats> findTop10BncPlayersByChat(long chatId) {
        return repo.findTop10ByChatIdOrderByBncScoreDesc(chatId);
    }

    @Override
    public List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids) {
        return repo.findByIdAndLoverIdIn(ids);
    }
}
