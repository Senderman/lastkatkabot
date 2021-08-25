package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoUserStatsService implements UserStatsService {

    private final UserStatsRepository repository;
    private final ChatUserService chatUserService;

    public MongoUserStatsService(UserStatsRepository repository, ChatUserService chatUserService) {
        this.repository = repository;
        this.chatUserService = chatUserService;
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

    @Override
    public List<Userstats> findTop10BncPlayersByChat(long chatId) {
        var userIds = chatUserService.findByChatId(chatId).stream().map(ChatUser::getUserId).toList();
        return repository.findTop10ByOrderByBncScoreDescByUserIdIn(userIds).stream().limit(10).toList();
    }
}
