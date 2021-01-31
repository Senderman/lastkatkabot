package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.service.CurrentTime;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class Pair implements CommandExecutor {

    private final static int TWO_WEEKS = (int) TimeUnit.DAYS.toSeconds(14);

    private final ApiRequests telegram;
    private final UserStatsRepository userStats;
    private final ChatUserRepository chatUsers;
    private final ChatInfoRepository chatInfoRepo;
    private final Love love;
    private final CurrentTime currentTime;
    private final Set<Long> runningChatPairsGenerations;
    private final ExecutorService threadPool;

    public Pair(
            ApiRequests telegram,
            UserStatsRepository userStats,
            ChatUserRepository chatUsers,
            ChatInfoRepository chatInfoRepo,
            Love love,
            CurrentTime currentTime,
            ExecutorService threadPool
    ) {
        this.telegram = telegram;
        this.userStats = userStats;
        this.chatUsers = chatUsers;
        this.chatInfoRepo = chatInfoRepo;
        this.love = love;
        this.currentTime = currentTime;
        this.runningChatPairsGenerations = Collections.synchronizedSet(new HashSet<>());
        this.threadPool = threadPool;
    }

    @Override
    public String getTrigger() {
        return "/pair";
    }

    @Override
    public String getDescription() {
        return "пара дня";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();

        if (message.isUserMessage()) {
            telegram.sendMessage(chatId, "Команду нельзя использовать в лс!");
            return;
        }

        // check if pair was generated today
        var chatInfo = chatInfoRepo.findById(chatId).orElse(new ChatInfo(chatId));
        var lastPairs = Optional.ofNullable(chatInfo.getLastPairs()).orElse(new ArrayList<>());
        var lastPairGenerationDate = Optional.ofNullable(chatInfo.getLastPairDate()).orElse(-1);
        int currentDay = Integer.parseInt(currentTime.getCurrentDay());

        if (!lastPairs.isEmpty() && lastPairGenerationDate == currentDay) {
            telegram.sendMessage(chatId, "Пара дня: " + lastPairs.get(0));
            return;
        }

        // clean inactive chat members
        forgetOldMembers(chatId);

        var usersForPair = chatUsers.sampleOfChat(chatId, 2);
        if (usersForPair.size() < 2) {
            telegram.sendMessage(chatId, "Недостаточно пользователей писало в чат за последние 2 недели!");
            return;
        }

        if (runningChatPairsGenerations.contains(chatId))
            return;

        runningChatPairsGenerations.add(chatId);

        String[] loveStrings = love.getRandomLoveStrings();
        var pairFuture = threadPool.submit(
                () -> generateNewPair(chatId, usersForPair.get(0), usersForPair.get(1)));

        threadPool.execute(() -> {
            try {
                sendRandomShitWithDelay(chatId, loveStrings, 1000L);
                var pair = pairFuture.get();
                chatInfo.setLastPairDate(currentDay);
                lastPairs.add(0, pair.toString());
                chatInfo.setLastPairs(lastPairs.stream().limit(10).collect(Collectors.toList()));
                chatInfoRepo.save(chatInfo);

                var text = String.format(loveStrings[loveStrings.length - 1],
                        Html.getUserLink(pair.getFirst()),
                        Html.getUserLink(pair.getSecond()));
                telegram.sendMessage(chatId, text);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                telegram.sendMessage(chatId, "...Упс, произошла ошибка. Попробуйте в другой раз");
            } finally {
                runningChatPairsGenerations.remove(chatId);
            }
        });

    }

    public PairData generateNewPair(long chatId, ChatUser firstUser, ChatUser secondUser) {

        var firstUserStats = userStats.findById(firstUser.getUserId());

        // if user has lover (see /marryme command) and the lover is in the chat, use him as lover
        // or else, use random user from chat members
        int loverId = firstUserStats
                .map(Userstats::getLoverId)
                .filter(id -> chatUsers.existsByChatIdAndUserId(chatId, id))
                .orElse(secondUser.getUserId());

        var firstMember = telegram.execute(Methods.getChatMember(chatId, firstUser.getUserId())).getUser();
        var secondMember = telegram.execute(Methods.getChatMember(chatId, loverId)).getUser();
        boolean isTrueLove = loverId == firstUserStats.map(Userstats::getLoverId).orElse(-1);

        return new PairData(firstMember, secondMember, isTrueLove);

    }

    private void forgetOldMembers(long chatId) {
        chatUsers.deleteByChatIdAndLastMessageDateLessThan(
                chatId,
                (int) (System.currentTimeMillis() / 1000 - TWO_WEEKS)
        );
    }


    private void sendRandomShitWithDelay(long chatId, String[] shit, long delay) {
        for (int i = 0; i < shit.length - 1; i++) {
            telegram.sendMessage(chatId, shit[i]);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class PairData {

        private final User first;
        private final User second;
        private final boolean isTrueLove;

        public PairData(User first, User second, boolean isTrueLove) {
            this.first = first;
            this.second = second;
            this.isTrueLove = isTrueLove;
        }

        public User getFirst() {
            return first;
        }

        public User getSecond() {
            return second;
        }

        private String getPairEmoji() {
            return isTrueLove ? "\uD83D\uDC96" : "❤️";
        }

        @Override
        public String toString() {
            return String.format("%s %s %s",
                    Html.htmlSafe(first.getFirstName()),
                    getPairEmoji(),
                    Html.htmlSafe(second.getFirstName())
            );
        }
    }

}
