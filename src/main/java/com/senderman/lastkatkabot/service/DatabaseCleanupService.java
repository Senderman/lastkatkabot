package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.repository.BncRepository;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.util.DbCleanupResults;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DatabaseCleanupService {

    private final static int TWO_WEEKS = (int) TimeUnit.DAYS.toSeconds(14);
    private final ChatUserRepository chatUserRepo;
    private final ChatInfoRepository chatInfoRepo;
    private final BncRepository bncRepo;
    private final MarriageRequestRepository marriageRequestRepo;

    public DatabaseCleanupService(ChatUserRepository chatUserRepo,
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
     * Deletes all users from DB without 2 week activity
     *
     * @return amount of users deleted
     */
    public long cleanInactiveUsers() {
        return chatUserRepo.deleteByLastMessageDateLessThan(twoWeeksBeforeNow());
    }

    public long cleanEmptyChats() {
        var chatsToClean = StreamSupport.stream(chatInfoRepo.findAll().spliterator(), false)
                .map(ChatInfo::getChatId)
                .filter(chatId -> !chatUserRepo.existsByChatId(chatId))
                .collect(Collectors.toList());
        return chatInfoRepo.deleteByChatIdIn(chatsToClean);
    }

    public long cleanOldBncGames() {
        return bncRepo.deleteByEditDateLessThan(twoWeeksBeforeNow());
    }

    public long cleanOldMarriageRequests() {
        return marriageRequestRepo.deleteByRequestDateLessThan(twoWeeksBeforeNow());
    }

    public DbCleanupResults cleanAll() {
        long users = cleanInactiveUsers();
        long chats = cleanEmptyChats();
        long bncGames = cleanOldBncGames();
        long marriageRequests = cleanOldBncGames();
        return new DbCleanupResults(users, chats, bncGames, marriageRequests);
    }

    private int twoWeeksBeforeNow() {
        return (int) (System.currentTimeMillis() / 1000 - TWO_WEEKS);
    }
}
