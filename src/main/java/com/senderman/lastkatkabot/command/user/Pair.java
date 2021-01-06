package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.exception.NotEnoughElementsException;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.TelegramHtmlUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class Pair implements CommandExecutor {

    private static final int TWO_WEEKS = (int) TimeUnit.DAYS.toSeconds(14);
    private static final int MIN_USERS = 3;

    private final ApiRequests telegram;
    private final UserStatsRepository userStats;
    private final ChatUserRepository chatUsers;
    private final ChatInfoRepository chatInfoRepo;
    private final Love love;
    private final Set<Long> runningChatPairsGenerations;

    public Pair(
            ApiRequests telegram,
            UserStatsRepository userStats,
            ChatUserRepository chatUsers,
            ChatInfoRepository chatInfoRepo,
            Love love
    ) {
        this.telegram = telegram;
        this.userStats = userStats;
        this.chatUsers = chatUsers;
        this.chatInfoRepo = chatInfoRepo;
        this.love = love;
        this.runningChatPairsGenerations = Collections.synchronizedSet(new HashSet<>());
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

        var chatInfo = chatInfoRepo.findById(chatId).orElse(new ChatInfo(chatId));
        var pairs = Optional.ofNullable(chatInfo.getLastPairs()).orElse(new ArrayList<>());
        var date = Optional.ofNullable(chatInfo.getLastPairDate()).orElse(-1);

        if (!pairs.isEmpty() && date + TWO_WEEKS > System.currentTimeMillis() / 1000) {
            telegram.sendMessage(chatId, "Пара дня: " + pairs.get(0));
            return;
        }

        if (runningChatPairsGenerations.contains(chatId))
            return;

        try {
            String[] loveStrings = love.getRandomLoveStrings();
            generateNewPair(chatInfo);
        } catch (NotEnoughElementsException e) {
            telegram.sendMessage(chatId,
                    "Недостаточно пользователей! Хотя бы " + MIN_USERS + " должны написать в чат!");
        }


    }

    public PairData generateNewPair(ChatInfo chatInfo) throws NotEnoughElementsException {
        var chatId = chatInfo.getChatId();
        forgetOldMembers(chatId);

        List<ChatUser> users = chatUsers.findAllByChatId(chatId);
        if (users.size() < 3) {
            throw new NotEnoughElementsException(3);
        }
        var random = ThreadLocalRandom.current();
        var firstUser = users.remove(random.nextInt(users.size()));
        var firstUserStats = userStats.findById(firstUser.getUserId());

        // if user has lover (see /marryme command) and the lover is in the chat, use him as lover
        // or else, use random user from chat members
        int loverId = firstUserStats
                .map(Userstats::getLoverId)
                .filter(id -> users.stream().anyMatch(u -> u.getUserId() == id))
                .orElse(users.get(random.nextInt(users.size())).getUserId());

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

        public boolean isTrueLove() {
            return isTrueLove;
        }

        private String getPairEmoji() {
            return isTrueLove ? "\uD83D\uDC96" : "❤️";
        }

        @Override
        public String toString() {
            return String.format("%s %s %s",
                    TelegramHtmlUtils.getUserLink(first),
                    getPairEmoji(),
                    TelegramHtmlUtils.getUserLink(second)
            );
        }

        public String toNoNotifyString() {
            return String.format("%s %s %s",
                    TelegramHtmlUtils.htmlSafe(first.getFirstName()),
                    getPairEmoji(),
                    TelegramHtmlUtils.htmlSafe(second.getFirstName())
            );
        }
    }

}
