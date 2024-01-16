package com.senderman.lastkatkabot.feature.cleanup.service;

import com.senderman.lastkatkabot.feature.bnc.repository.BncGameMessageRepository;
import com.senderman.lastkatkabot.feature.bnc.repository.BncRepository;
import com.senderman.lastkatkabot.feature.cake.repository.CakeRepository;
import com.senderman.lastkatkabot.feature.chatsettings.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.feature.love.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.feature.tracking.repository.ChatUserRepository;
import com.senderman.lastkatkabot.feature.userstats.repository.UserStatsRepository;
import jakarta.inject.Singleton;

@Singleton
public class H2CleanupService extends DatabaseCleanupService {

    private final ChatUserRepository chatUserRepo;
    private final ChatInfoRepository chatInfoRepo;
    private final BncRepository bncRepo;
    private final BncGameMessageRepository bncGameMessageRepo;
    private final MarriageRequestRepository marriageRequestRepo;
    private final CakeRepository cakeRepo;
    private final UserStatsRepository userStatsRepo;

    public H2CleanupService(
            ChatUserRepository chatUserRepo,
            ChatInfoRepository chatInfoRepo,
            BncRepository bncRepo,
            BncGameMessageRepository bncGameMessageRepo,
            MarriageRequestRepository marriageRequestRepo,
            CakeRepository cakeRepo,
            UserStatsRepository userStatsRepo
    ) {
        this.chatUserRepo = chatUserRepo;
        this.chatInfoRepo = chatInfoRepo;
        this.bncRepo = bncRepo;
        this.bncGameMessageRepo = bncGameMessageRepo;
        this.marriageRequestRepo = marriageRequestRepo;
        this.cakeRepo = cakeRepo;
        this.userStatsRepo = userStatsRepo;
    }


    /**
     * Deletes all users from CHAT_USER table without given activity period
     */
    @Override
    public void cleanInactiveChatUsers() {
        chatUserRepo.deleteByLastMessageDateLessThan(inactivePeriodGeneral());
    }

    @Override
    public void cleanEmptyChats() {
        chatInfoRepo.deleteEmptyChats();
    }

    @Override
    public void cleanOldBncGames() {
        bncRepo.deleteByEditDateLessThan(inactivePeriodGeneralTs());
        bncGameMessageRepo.deleteOrphanMessages();
    }

    @Override
    public void cleanOldMarriageRequests() {
        marriageRequestRepo.deleteByCreatedAtLessThan(inactivePeriodGeneralTs());
    }

    @Override
    public void cleanOldCakes() {
        cakeRepo.deleteByCreatedAtLessThan(inactivePeriodCake());
    }

    @Override
    public void cleanInactiveUserStats() {
        userStatsRepo.deleteByUpdatedAtLessThan(inactivePeriodUserStats());
    }

}
