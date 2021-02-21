package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.DatabaseCleanupService;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.repository.BncRepository;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.util.DbCleanupResults;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MongoCleanupService implements DatabaseCleanupService {

    private final ChatUserRepository chatUserRepo;
    private final ChatInfoRepository chatInfoRepo;
    private final BncRepository bncRepo;
    private final MarriageRequestRepository marriageRequestRepo;

    public MongoCleanupService(ChatUserRepository chatUserRepo,
                               ChatInfoRepository chatInfoRepo,
                               BncRepository bncRepo,
                               MarriageRequestRepository marriageRequestRepo
    ) {
        this.chatUserRepo = chatUserRepo;
        this.chatInfoRepo = chatInfoRepo;
        this.bncRepo = bncRepo;
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
        var chatsToClean = StreamSupport.stream(chatInfoRepo.findAll().spliterator(), false)
                .map(ChatInfo::getChatId)
                .filter(chatId -> !chatUserRepo.existsByChatId(chatId))
                .collect(Collectors.toList());
        return chatInfoRepo.deleteByChatIdIn(chatsToClean);
    }

    @Override
    public long cleanOldBncGames() {
        return bncRepo.deleteByEditDateLessThan(DatabaseCleanupService.inactivePeriod());
    }

    @Override
    public long cleanOldMarriageRequests() {
        return marriageRequestRepo.deleteByRequestDateLessThan(DatabaseCleanupService.inactivePeriod());
    }

    @Override
    public DbCleanupResults cleanAll() {
        long users = cleanInactiveUsers();
        long chats = cleanEmptyChats();
        long bncGames = cleanOldBncGames();
        long marriageRequests = cleanOldMarriageRequests();
        return new DbCleanupResults(users, chats, bncGames, marriageRequests);
    }
}
