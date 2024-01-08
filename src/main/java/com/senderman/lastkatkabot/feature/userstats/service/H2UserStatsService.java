package com.senderman.lastkatkabot.feature.userstats.service;

import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import com.senderman.lastkatkabot.feature.tracking.service.ChatUserService;
import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import com.senderman.lastkatkabot.feature.userstats.repository.UserStatsRepository;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;

@Singleton
public class H2UserStatsService implements UserStatsService {

    private final UserStatsRepository repo;
    private final ChatUserService chatUserService;

    public H2UserStatsService(UserStatsRepository repo, ChatUserService chatUserService) {
        this.repo = repo;
        this.chatUserService = chatUserService;
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
        var userIds = chatUserService.findByChatId(chatId).stream().map(ChatUser::getUserId).toList();
        if (userIds.isEmpty())
            return List.of();
        return repo.findTop10ByUserIdInOrderByBncScoreDesc(userIds);
    }

    @Override
    public List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids) {
        return repo.findByIdAndLoverIdIn(ids);
    }
}
