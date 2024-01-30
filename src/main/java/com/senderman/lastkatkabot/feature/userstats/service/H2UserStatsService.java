package com.senderman.lastkatkabot.feature.userstats.service;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import com.senderman.lastkatkabot.feature.userstats.repository.UserStatsRepository;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
public class H2UserStatsService implements UserStatsService {

    private final UserStatsRepository repo;

    public H2UserStatsService(UserStatsRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserStats findById(long userId) {
        return repo.findById(userId).orElseGet(() -> new UserStats(userId, ""));
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

    @Override
    public void updateOrCreateByUserId(long userId, String name, @Nullable String locale) {
        if (repo.existsById(userId))
            repo.updateByUserId(userId, name, locale);
        else {
            var user = new UserStats(userId, name);
            user.setLocale(locale);
            repo.save(user);
        }
    }

    @Override
    public List<UserStats> findByChatId(long chatId) {
        return repo.findByChatId(chatId);
    }

    @Override
    public List<UserStats> findRandomUsersOfChat(long chatId, int amount) {
        return repo.findRandomUsersOfChat(chatId, amount);
    }

    @Override
    public Optional<UserStats> findByChatIdAndUserId(long chatId, long userId) {
        return repo.findByChatIdAndUserId(chatId, userId);
    }
}
