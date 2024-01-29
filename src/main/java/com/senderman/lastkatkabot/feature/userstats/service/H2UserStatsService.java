package com.senderman.lastkatkabot.feature.userstats.service;

import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import com.senderman.lastkatkabot.feature.userstats.repository.UserStatsRepository;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
public class H2UserStatsService implements UserStatsService {

    private final UserStatsRepository repo;
    private final BotConfig botConfig;

    public H2UserStatsService(UserStatsRepository repo, BotConfig botConfig) {
        this.repo = repo;
        this.botConfig = botConfig;
    }

    @Override
    public UserStats findById(long userId, String name, String locale) {
        return repo.findById(userId)
                .map(u -> {
                    u.setName(name);
                    return u;
                })
                .orElseGet(() -> new UserStats(userId, name, getLocaleOrSupported(locale)));
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
    public void updateOrCreateByUserId(long userId, String name, String locale) {
        if (repo.existsById(userId))
            repo.updateByUserId(userId, name);
        else {
            var user = new UserStats(userId, name, locale);
            user.setLocale(getLocaleOrSupported(locale));
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

    private String getLocaleOrSupported(String locale) {
        return botConfig.getLocale().getSupportedLocales().contains(locale)
                ? locale
                : botConfig.getLocale().getDefaultLocale();
    }
}
