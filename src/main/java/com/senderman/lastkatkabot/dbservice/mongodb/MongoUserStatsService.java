package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.model.UserStats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class MongoUserStatsService implements UserStatsService {

    private final UserStatsRepository repository;
    private final ChatUserService chatUserService;

    public MongoUserStatsService(UserStatsRepository repository, ChatUserService chatUserService) {
        this.repository = repository;
        this.chatUserService = chatUserService;
    }

    @Override
    public UserStats findById(long userId) {
        return repository.findById(userId).orElseGet(() -> new UserStats(userId));
    }

    @Override
    public UserStats save(UserStats userstats) {
        return repository.update(userstats);
    }

    @Override
    public Iterable<UserStats> saveAll(Iterable<UserStats> userstats) {
        return repository.updateAll(userstats);
    }

    @Override
    public List<UserStats> findTop10BncPlayers() {
        return repository.findTop10OrderByBncScoreDesc();
    }

    @Override
    public List<UserStats> findTop10BncPlayersByChat(long chatId) {
        var userIds = chatUserService.findByChatId(chatId).stream().map(ChatUser::getUserId).toList();
        return repository.findTop10ByUserIdInOrderByBncScoreDesc(userIds).stream()
                .limit(10)
                .toList();
    }
}
