package com.senderman.lastkatkabot.feature.data.service;

import com.senderman.lastkatkabot.feature.bnc.repository.BncGameMessageRepository;
import com.senderman.lastkatkabot.feature.bnc.repository.BncRepository;
import com.senderman.lastkatkabot.feature.cake.repository.CakeRepository;
import com.senderman.lastkatkabot.feature.chatsettings.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.feature.genshin.repository.GenshinChatUserRepository;
import com.senderman.lastkatkabot.feature.genshin.repository.GenshinUserInventoryItemRepository;
import com.senderman.lastkatkabot.feature.love.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.feature.tracking.repository.ChatUserRepository;
import com.senderman.lastkatkabot.feature.userstats.repository.UserStatsRepository;
import com.senderman.lastkatkabot.handler.BotHandler;
import jakarta.inject.Singleton;

@Singleton
public class PgsqlCleanupService extends DatabaseCleanupService {

    private final ChatUserRepository chatUserRepo;
    private final ChatInfoRepository chatInfoRepo;
    private final BncRepository bncRepo;
    private final BncGameMessageRepository bncGameMessageRepo;
    private final MarriageRequestRepository marriageRequestRepo;
    private final CakeRepository cakeRepo;
    private final UserStatsRepository userStatsRepo;
    private final GenshinChatUserRepository genshinChatUserRepo;
    private final GenshinUserInventoryItemRepository genshinUserInventoryItemRepo;

    public PgsqlCleanupService(
            ChatUserRepository chatUserRepo,
            ChatInfoRepository chatInfoRepo,
            BncRepository bncRepo,
            BncGameMessageRepository bncGameMessageRepo,
            MarriageRequestRepository marriageRequestRepo,
            CakeRepository cakeRepo,
            UserStatsRepository userStatsRepo,
            GenshinChatUserRepository genshinChatUserRepo,
            GenshinUserInventoryItemRepository genshinUserInventoryItemRepo,
            BotHandler botHandler
    ) {
        super(botHandler);
        this.chatUserRepo = chatUserRepo;
        this.chatInfoRepo = chatInfoRepo;
        this.bncRepo = bncRepo;
        this.bncGameMessageRepo = bncGameMessageRepo;
        this.marriageRequestRepo = marriageRequestRepo;
        this.cakeRepo = cakeRepo;
        this.userStatsRepo = userStatsRepo;
        this.genshinChatUserRepo = genshinChatUserRepo;
        this.genshinUserInventoryItemRepo = genshinUserInventoryItemRepo;
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
        userStatsRepo.deleteOldUsers(inactivePeriodUserStats());
        userStatsRepo.updateNonExistentLovers();
    }

    @Override
    public void cleanOldGenshinData() {
        genshinChatUserRepo.deleteByUpdatedAtLessThan(inactivePeriodUserStats());
        genshinUserInventoryItemRepo.deleteInactiveInventories();
    }

}
