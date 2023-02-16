package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.BncGameMessageService;
import com.senderman.lastkatkabot.dbservice.DatabaseCleanupService;
import com.senderman.lastkatkabot.model.BncGameSave;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.repository.BncRepository;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.util.DbCleanupResults;
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

    public MongoCleanupService(
            ChatUserRepository chatUserRepo,
            ChatInfoRepository chatInfoRepo,
            BncRepository bncRepo,
            BncGameMessageService bncGameMessageService,
            MarriageRequestRepository marriageRequestRepo
    ) {
        this.chatUserRepo = chatUserRepo;
        this.chatInfoRepo = chatInfoRepo;
        this.bncRepo = bncRepo;
        this.bncGameMessageService = bncGameMessageService;
        this.marriageRequestRepo = marriageRequestRepo;
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
                .stream(chatInfoRepo.findAll().spliterator(), false)
                .map(ChatInfo::getChatId)
                .collect(Collectors.toCollection(ArrayList::new));
        var chatsWithUsersIds = chatUserRepo.findDistinctUserId();
        chatIds.removeAll(chatsWithUsersIds);
        return chatInfoRepo.deleteByChatIdIn(chatIds);
    }

    @Override
    public long cleanOldBncGames() {
        var deletedGames = bncRepo.deleteByEditDateLessThan(DatabaseCleanupService.inactivePeriod());
        var gameIds = deletedGames.stream().map(BncGameSave::getId).collect(Collectors.toList());
        if (!gameIds.isEmpty())
            bncGameMessageService.deleteByGameIdIn(gameIds);
        return deletedGames.size();
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
