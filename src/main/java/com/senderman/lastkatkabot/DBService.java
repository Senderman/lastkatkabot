package com.senderman.lastkatkabot;

import com.senderman.lastkatkabot.tempobjects.BnCPlayer;
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DBService {

    void initStats(int id);

    void incTotalDuels(int id);

    void incDuelWins(int id);

    void incBNCWins(int id, int points);

    Map<String, Integer> getStats(int id);

    List<BnCPlayer> getTop(); // return map of <id, score> sorted by descending order

    void setUserCity(int id, String city);

    String getUserCity(int id);


    void addTgUser(int id, COLLECTION_TYPE type);

    void removeTGUser(int id, COLLECTION_TYPE type);

    Set<Integer> getTgUsersFromList(COLLECTION_TYPE type);

    Set<Integer> getTgUsersIds(COLLECTION_TYPE type);

    Set<Integer> getAllUsersIds();


    void addUserToChatDB(Message message);

    void removeUserFromChatDB(int userId, long chatId);

    List<Integer> getChatMemebersIds(long chatId);

    void removeOldUsers(long chatId, int date);


    Map<Long, BullsAndCowsGame> getBnCGames();

    void saveBncGame(long chatId, BullsAndCowsGame game);

    void deleteBncGame(long chatId);


    int getTournamentMessageId();

    void setTournamentMessage(int messageId);


    void addAllowedChat(long chatId, String title);

    Set<Long> getAllowedChatsSet();

    void updateChatId(long oldChatId, long newChatId);

    void updateTitle(long chatId, String title);

    void removeAllowedChat(long chatId);

    void setPair(long chatId, String pair);

    Map<Long, String> getAllowedChats();

    void cleanup();


    boolean pairExistsToday(long chatId);

    enum COLLECTION_TYPE {ADMINS, PREMIUM, BLACKLIST}

    String getPairOfTheDay(long chatId);

    String getPairsHistory(long chatId);


    // will be removed after a while

    void incRavenMessages(int date);

    int getRavenMessages();

    void updateRavenRecord();

    void incInterruptions();

    void redInterruptions();

    int getInterruptions();

    int getLastRavenDate();

    int getRavenRecord();

}