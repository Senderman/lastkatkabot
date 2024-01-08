package com.senderman.lastkatkabot.feature.cleanup.service;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import com.senderman.lastkatkabot.feature.bnc.repository.BncRepository;
import com.senderman.lastkatkabot.feature.bnc.service.BncGameMessageService;
import com.senderman.lastkatkabot.feature.cake.repository.CakeRepository;
import com.senderman.lastkatkabot.feature.chatsettings.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.feature.cleanup.model.DbCleanupResults;
import com.senderman.lastkatkabot.feature.love.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.feature.tracking.repository.ChatUserRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class H2CleanupService implements DatabaseCleanupService {

    private final ChatUserRepository chatUserRepo;
    private final ChatInfoRepository chatInfoRepo;
    private final BncRepository bncRepo;
    private final BncGameMessageService bncGameMessageService;
    private final MarriageRequestRepository marriageRequestRepo;
    private final CakeRepository cakeRepo;

    public H2CleanupService(
            ChatUserRepository chatUserRepo,
            ChatInfoRepository chatInfoRepo,
            BncRepository bncRepo,
            BncGameMessageService bncGameMessageService,
            MarriageRequestRepository marriageRequestRepo,
            CakeRepository cakeRepo
    ) {
        this.chatUserRepo = chatUserRepo;
        this.chatInfoRepo = chatInfoRepo;
        this.bncRepo = bncRepo;
        this.bncGameMessageService = bncGameMessageService;
        this.marriageRequestRepo = marriageRequestRepo;
        this.cakeRepo = cakeRepo;
    }


    /**
     * Deletes all users from DB without given activity period
     *
     * @return amount of users deleted
     */
    @Override
    public long cleanInactiveUsers() {
        return chatUserRepo.deleteByLastMessageDateLessThan(DatabaseCleanupService.inactivePeriodGeneral());
    }

    @Override
    public long cleanEmptyChats() {
        List<Long> chatIds = new ArrayList<>(chatInfoRepo.findDistinctchatId());
        List<Long> chatsWithUsersIds = chatUserRepo.findDistinctChatId();
        chatsWithUsersIds.forEach(chatIds::remove);
        return chatInfoRepo.deleteByChatIdIn(chatIds);
    }

    @Override
    public long cleanOldBncGames() {
        var gamesToDelete = bncRepo.findByEditDateLessThan(DatabaseCleanupService.inactivePeriodGeneralTs());
        bncRepo.deleteAll(gamesToDelete);
        var gameIds = gamesToDelete.stream().map(BncGameSave::getId).collect(Collectors.toList());
        if (!gameIds.isEmpty())
            bncGameMessageService.deleteByGameIdIn(gameIds);
        return gameIds.size();
    }

    @Override
    public long cleanOldMarriageRequests() {
        return marriageRequestRepo.deleteByCreatedAtLessThan(DatabaseCleanupService.inactivePeriodGeneralTs());
    }

    @Override
    public long cleanOldCakes() {
        return cakeRepo.deleteByCreatedAtLessThan(DatabaseCleanupService.inactivePeriodCake());
    }

    @Override
    @Scheduled(fixedDelay = "2h")
    public DbCleanupResults cleanAll() {
        long users = cleanInactiveUsers();
        long chats = cleanEmptyChats();
        long bncGames = cleanOldBncGames();
        long marriageRequests = cleanOldMarriageRequests();
        long cakes = cleanOldCakes();
        return new DbCleanupResults(users, chats, bncGames, marriageRequests, cakes);
    }
}
