package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.DatabaseCleanupService;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.BncRepository;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.util.DbCleanupResults;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoCleanupService implements DatabaseCleanupService {

    private final ChatUserRepository chatUserRepo;
    private final ChatInfoRepository chatInfoRepo;
    private final BncRepository bncRepo;
    private final MarriageRequestRepository marriageRequestRepo;
    private final MongoTemplate mongoTemplate;

    public MongoCleanupService(ChatUserRepository chatUserRepo,
                               ChatInfoRepository chatInfoRepo,
                               BncRepository bncRepo,
                               MarriageRequestRepository marriageRequestRepo,
                               MongoTemplate mongoTemplate) {
        this.chatUserRepo = chatUserRepo;
        this.chatInfoRepo = chatInfoRepo;
        this.bncRepo = bncRepo;
        this.marriageRequestRepo = marriageRequestRepo;
        this.mongoTemplate = mongoTemplate;
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
        var chatIds = mongoTemplate.findDistinct("_id", ChatInfo.class, Long.class);
        var chatsWithUsersIds = mongoTemplate.findDistinct("chatId", ChatUser.class, Long.class);
        chatIds.removeAll(chatsWithUsersIds);
        return chatInfoRepo.deleteByChatIdIn(chatIds);
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
