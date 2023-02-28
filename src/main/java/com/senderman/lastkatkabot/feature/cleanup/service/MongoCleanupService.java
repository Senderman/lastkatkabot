package com.senderman.lastkatkabot.feature.cleanup.service;

import com.mongodb.client.MongoDatabase;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import com.senderman.lastkatkabot.feature.bnc.repository.BncRepository;
import com.senderman.lastkatkabot.feature.bnc.service.BncGameMessageService;
import com.senderman.lastkatkabot.feature.chatsettings.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.feature.cleanup.model.DbCleanupResults;
import com.senderman.lastkatkabot.feature.love.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.feature.tracking.repository.ChatUserRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class MongoCleanupService implements DatabaseCleanupService {

    private final ChatUserRepository chatUserRepo;
    private final ChatInfoRepository chatInfoRepo;
    private final BncRepository bncRepo;
    private final BncGameMessageService bncGameMessageService;
    private final MarriageRequestRepository marriageRequestRepo;
    private final MongoDatabase mongoDatabase;

    public MongoCleanupService(
            ChatUserRepository chatUserRepo,
            ChatInfoRepository chatInfoRepo,
            BncRepository bncRepo,
            BncGameMessageService bncGameMessageService,
            MarriageRequestRepository marriageRequestRepo,
            MongoDatabase mongoDatabase
    ) {
        this.chatUserRepo = chatUserRepo;
        this.chatInfoRepo = chatInfoRepo;
        this.bncRepo = bncRepo;
        this.bncGameMessageService = bncGameMessageService;
        this.marriageRequestRepo = marriageRequestRepo;
        this.mongoDatabase = mongoDatabase;
    }


    /**
     * Deletes all users from DB without given activity period
     *
     * @return amount of users deleted
     */
    @Override
    public long cleanInactiveUsers() {
        return chatUserRepo.deleteByLastMessageDateLessThan(DatabaseCleanupService.inactivePeriod());
    }

    @Override
    public long cleanEmptyChats() {
        var chatIds = StreamSupport
                .stream(mongoDatabase.getCollection("chatInfo").distinct("_id", Long.class).spliterator(), false)
                .collect(Collectors.toCollection(ArrayList::new));
        var chatsWithUsersIds = mongoDatabase.getCollection("chatUser").distinct("chatId", Long.class);
        chatsWithUsersIds.forEach(chatIds::remove);
        return chatInfoRepo.deleteByChatIdIn(chatIds);
    }

    @Override
    public long cleanOldBncGames() {
        var gamesToDelete = bncRepo.findByEditDateLessThan(DatabaseCleanupService.inactivePeriod());
        bncRepo.deleteAll(gamesToDelete);
        var gameIds = gamesToDelete.stream().map(BncGameSave::getId).collect(Collectors.toList());
        if (!gameIds.isEmpty())
            bncGameMessageService.deleteByGameIdIn(gameIds);
        return gameIds.size();
    }

    @Override
    public long cleanOldMarriageRequests() {
        return marriageRequestRepo.deleteByRequestDateLessThan(DatabaseCleanupService.inactivePeriod());
    }

    @Override
    @Scheduled(fixedDelay = "2h")
    public DbCleanupResults cleanAll() {
        long users = cleanInactiveUsers();
        long chats = cleanEmptyChats();
        long bncGames = cleanOldBncGames();
        long marriageRequests = cleanOldMarriageRequests();
        return new DbCleanupResults(users, chats, bncGames, marriageRequests);
    }
}
