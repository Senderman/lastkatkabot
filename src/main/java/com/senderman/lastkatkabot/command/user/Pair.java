package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
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

    private final CommonAbsSender telegram;
    private final UserStatsService userStats;
    private final ChatUserRepository chatUsers;
    private final ChatInfoRepository chatInfoRepo;
    private final Love love;
    private final CurrentTime currentTime;
    private final Set<Long> runningChatPairsGenerations;
    private final ExecutorService threadPool;

    public Pair(
            CommonAbsSender telegram,
            UserStatsService userStats,
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
            Methods.sendMessage(chatId, "Команду нельзя использовать в лс!").callAsync(telegram);
            return;
        }

        // check if pair was generated today
        var chatInfo = chatInfoRepo.findById(chatId).orElseGet(() -> new ChatInfo(chatId));
        var lastPairs = Optional.ofNullable(chatInfo.getLastPairs()).orElseGet(ArrayList::new);
        var lastPairGenerationDate = Objects.requireNonNullElse(chatInfo.getLastPairDate(), -1);
        int currentDay = Integer.parseInt(currentTime.getCurrentDay());

        if (!lastPairs.isEmpty() && lastPairGenerationDate == currentDay) {
            Methods.sendMessage(chatId, "Пара дня: " + lastPairs.get(0)).callAsync(telegram);
            return;
        }

        // clean inactive chat members
        forgetOldMembers(chatId);

        var usersForPair = chatUsers.sampleOfChat(chatId, 2);
        if (usersForPair.size() < 2) {
            Methods.sendMessage(chatId, "Недостаточно пользователей писало в чат за последние 2 недели!").callAsync(telegram);
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
                Methods.sendMessage(chatId, text).callAsync(telegram);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Methods.sendMessage(chatId, "...Упс, произошла ошибка. Попробуйте в другой раз").callAsync(telegram);
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
        if (stats1.getLoverId() != null && chatUsers.existsByChatIdAndUserId(chatId, stats1.getLoverId())) {
            loverId = stats1.getLoverId();
            isTrueLove = true;
        } else {
            loverId = secondUser.getUserId();
            isTrueLove = false;
        }

        var member1 = Methods.getChatMember(chatId, firstUser.getUserId()).call(telegram).getUser();
        var member2 = Methods.getChatMember(chatId, loverId).call(telegram).getUser();

        return new PairData(member1, member2, isTrueLove);

    }

    private void forgetOldMembers(long chatId) {
        chatUsers.deleteByChatIdAndLastMessageDateLessThan(
                chatId,
                (int) (System.currentTimeMillis() / 1000 - TWO_WEEKS)
        );
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
