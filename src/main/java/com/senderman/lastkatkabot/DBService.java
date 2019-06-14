package com.senderman.lastkatkabot;

import com.senderman.lastkatkabot.TempObjects.BullsAndCowsGame;
import com.senderman.lastkatkabot.TempObjects.TgUser;
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

    void addTgUser(int id, String name, COLLECTION_TYPE type);

    void removeTGUser(int id, COLLECTION_TYPE type);

    void setUserLocale(int id, String locale);

    String getUserLocale(int id);

    Set<TgUser> getTgUsers(COLLECTION_TYPE type);

    Set<Integer> getTgUsersIds(COLLECTION_TYPE type);

    Set<Integer> getAllUsersIds();


    void addUserToChatDB(Message message);

    void removeUserFromChatDB(int userId, long chatId);

    Set<Long> getAllowedChatsSet();

    List<Integer> getChatMemebersIds(long chatId);

    void removeOldUsers(long chatId, long date);

    void setChatLocale(long chatId, String locale);

    String getChatLocale(long chatId);


    Map<Long, BullsAndCowsGame> getBnCGames();

    void saveBncGame(long chatId, BullsAndCowsGame game);

    void deleteBncGame(long chatId);


    int getTournamentMessageId();

    void setTournamentMessage(int messageId);


    void addAllowedChat(long chatId, String title);

    void updateTitle(long chatId, String title);

    void removeAllowedChat(long chatId);

    void setPair(long chatId, String pair);

    Map<Long, String> getAllowedChats();


    boolean pairExistsToday(long chatId);

    enum COLLECTION_TYPE {ADMINS, PREMIUM, BLACKLIST}

    String getPairOfTheDay(long chatId);

    String getPairsHistory(long chatId);

}