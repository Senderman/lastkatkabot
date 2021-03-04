package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.service.CurrentTime;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
public class Pair implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final UserStatsService userStats;
    private final ChatUserService chatUsers;
    private final ChatInfoService chatInfoService;
    private final Love love;
    private final CurrentTime currentTime;
    private final Set<Long> runningChatPairsGenerations;
    private final ExecutorService threadPool;

    public Pair(
            CommonAbsSender telegram,
            UserStatsService userStats,
            ChatUserService chatUsers,
            ChatInfoService chatInfoService,
            Love love,
            CurrentTime currentTime,
            ExecutorService threadPool
    ) {
        this.telegram = telegram;
        this.userStats = userStats;
        this.chatUsers = chatUsers;
        this.chatInfoService = chatInfoService;
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
            Methods.sendMessage(chatId, "Команду нельзя использовать в лс!").callAsync(telegram);
            return;
        }

        // check if pair was generated today
        var chatInfo = chatInfoService.findById(chatId);
        List<String> lastPairs = Objects.requireNonNullElseGet(chatInfo.getLastPairs(), ArrayList::new);
        var lastPairGenerationDate = Objects.requireNonNullElse(chatInfo.getLastPairDate(), -1);
        int currentDay = Integer.parseInt(currentTime.getCurrentDay());

        if (!lastPairs.isEmpty() && lastPairGenerationDate == currentDay) {
            Methods.sendMessage(chatId, "Пара дня: " + lastPairs.get(0)).callAsync(telegram);
            return;
        }

        // clean inactive chat members
        chatUsers.deleteInactiveChatUsers(chatId);

        if (runningChatPairsGenerations.contains(chatId))
            return;

        runningChatPairsGenerations.add(chatId);

        // start chat flooding to make users wait for pair generation
        String[] loveStrings = love.getRandomLoveStrings();
        var floodFuture = threadPool.submit(() -> sendRandomShitWithDelay(chatId, loveStrings, 1000L));


        threadPool.execute(() -> {
            var usersForPair = chatUsers.getTwoOrLessUsersOfChat(chatId);
            if (usersForPair.size() < 2) {
                Methods.sendMessage(chatId, "Недостаточно пользователей писало в чат за последние 2 недели!").callAsync(telegram);
                return;
            }

            try {
                var pair = generateNewPair(chatId, usersForPair.get(0), usersForPair.get(1));
                chatInfo.setLastPairDate(currentDay);
                lastPairs.add(0, pair.toString());
                chatInfo.setLastPairs(lastPairs.stream().limit(10).collect(Collectors.toList()));
                chatInfoService.save(chatInfo);

                var text = String.format(loveStrings[loveStrings.length - 1],
                        Html.getUserLink(pair.getFirst()),
                        Html.getUserLink(pair.getSecond()));
                // wait while flood ends to prevent race condition
                floodFuture.get();
                Methods.sendMessage(chatId, text).callAsync(telegram);
            } catch (InterruptedException | ExecutionException e) {
                floodFuture.cancel(true);
                e.printStackTrace();
                chatUsers.delete(usersForPair.get(0));
                chatUsers.delete(usersForPair.get(0));
                Methods.sendMessage(chatId, "...Упс, произошла ошибка. Попробуйте еще раз").callAsync(telegram);
            } finally {
                runningChatPairsGenerations.remove(chatId);
            }
        });
    }

    public PairData generateNewPair(long chatId, ChatUser firstUser, ChatUser secondUser) {

        var stats1 = userStats.findById(firstUser.getUserId());

        // if user has lover (see /marryme command) and the lover is in the chat, use him as lover
        // or else, use random user from chat members
        int loverId;
        boolean isTrueLove;
        if (stats1.getLoverId() != null && chatUsers.chatHasUser(chatId, stats1.getLoverId())) {
            loverId = stats1.getLoverId();
            isTrueLove = true;
        } else {
            loverId = secondUser.getUserId();
            isTrueLove = false;
        }

        var member1 = getUser(chatId, firstUser.getUserId());
        var member2 = getUser(chatId, loverId);

        return new PairData(member1, member2, isTrueLove);

    }

    private User getUser(long chatId, int userId) {
        return Methods.getChatMember(chatId, userId).call(telegram).getUser();
    }

    private void sendRandomShitWithDelay(long chatId, String[] shit, long delay) {
        for (int i = 0; i < shit.length - 1; i++) {
            Methods.sendMessage(chatId, shit[i]).callAsync(telegram);
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
            return isTrueLove ? "💖" : "❤️";
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
