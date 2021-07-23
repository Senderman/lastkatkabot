package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
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
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class PairCommand implements CommandExecutor {

    private final UserStatsService userStats;
    private final ChatUserService chatUsers;
    private final ChatInfoService chatInfoService;
    private final Love love;
    private final CurrentTime currentTime;
    private final Set<Long> runningChatPairsGenerations;
    private final ExecutorService threadPool;

    public PairCommand(
            UserStatsService userStats,
            ChatUserService chatUsers,
            ChatInfoService chatInfoService,
            Love love,
            CurrentTime currentTime,
            ExecutorService threadPool
    ) {
        this.userStats = userStats;
        this.chatUsers = chatUsers;
        this.chatInfoService = chatInfoService;
        this.love = love;
        this.currentTime = currentTime;
        this.runningChatPairsGenerations = Collections.synchronizedSet(new HashSet<>());
        this.threadPool = threadPool;
    }

    @Override
    public String command() {
        return "/pair";
    }

    @Override
    public String getDescription() {
        return "пара дня";
    }

    @Override
    public void accept(MessageContext ctx) {
        long chatId = ctx.chatId();

        if (ctx.message().isUserMessage()) {
            ctx.replyToMessage("Команду нельзя использовать в лс!").callAsync(ctx.sender);
            return;
        }

        // check if pair was generated today
        var chatInfo = chatInfoService.findById(chatId);
        List<String> lastPairs = Objects.requireNonNullElseGet(chatInfo.getLastPairs(), ArrayList::new);
        var lastPairGenerationDate = Objects.requireNonNullElse(chatInfo.getLastPairDate(), -1);
        int currentDay = Integer.parseInt(currentTime.getCurrentDay());

        if (!lastPairs.isEmpty() && lastPairGenerationDate == currentDay) {
            ctx.reply("Пара дня: " + lastPairs.get(0)).callAsync(ctx.sender);
            return;
        }

        if (!runningChatPairsGenerations.add(chatId)) return;

        // clean inactive chat members
        chatUsers.deleteInactiveChatUsers(chatId);

        if (chatUsers.countByChatId(chatId) < 2) {
            ctx.reply("Недостаточно пользователей писало в чат за последние 2 недели!").callAsync(ctx.sender);
            runningChatPairsGenerations.remove(chatId);
            return;
        }

        // start chat flooding to make users wait for pair generation
        String[] loveStrings = love.getRandomLoveStrings();
        Future<?> floodFuture = threadPool.submit(() -> sendRandomShitWithDelay(chatId, loveStrings, ctx.sender));

        threadPool.execute(() -> {
            try {
                PairData pair;
                try {
                    pair = generateNewPair(chatId, ctx.sender);
                } catch (NotEnoughUsersException e) {
                    floodFuture.cancel(true);
                    ctx.reply("Ой, а у вас мало пользователей в чате доступно... Придется остановить генерацию пары").callAsync(ctx.sender);
                    return;
                }

                // save new generated pair and date to DB
                chatInfo.setLastPairDate(currentDay);
                lastPairs.add(0, pair.toString());
                chatInfo.setLastPairs(lastPairs.stream().limit(10).collect(Collectors.toList()));
                chatInfoService.save(chatInfo);

                var text = String.format(loveStrings[loveStrings.length - 1],
                        Html.getUserLink(pair.first),
                        Html.getUserLink(pair.second));
                // wait while flood ends to prevent race condition
                floodFuture.get();
                ctx.reply(text).callAsync(ctx.sender);
            } catch (InterruptedException | ExecutionException e) {
                floodFuture.cancel(true);
                e.printStackTrace();
                ctx.reply("...Упс, произошла ошибка. Попробуйте еще раз").callAsync(ctx.sender);
            } finally {
                runningChatPairsGenerations.remove(chatId);
            }
        });
    }

    private PairData generateNewPair(long chatId, CommonAbsSender telegram) throws NotEnoughUsersException {

        final int USERS_REQUIRED = 2;

        List<ChatUser> usersForPair;
        while (true) {
            // request two random users from db
            usersForPair = chatUsers.getTwoOrLessUsersOfChat(chatId);
            if (usersForPair.size() < USERS_REQUIRED)
                throw new NotEnoughUsersException(usersForPair.size(), USERS_REQUIRED);

            var chatUser1 = usersForPair.get(0);
            var chatUser2 = usersForPair.get(1);
            // check that these users are available through getChatMember method
            // and that their name is not blank
            // if at least one of the users is not available, continue the while loop
            User user1, user2;
            try {
                user1 = getUserFromChatMember(chatId, chatUser1.getUserId(), telegram);
                validateNameIsNotBlank(user1);
            } catch (NoChatMemberException | BlankNameException e) {
                chatUsers.delete(chatUser1);
                continue;
            }
            try {
                user2 = getUserFromChatMember(chatId, chatUser2.getUserId(), telegram);
                validateNameIsNotBlank(user2);
            } catch (NoChatMemberException | BlankNameException e) {
                chatUsers.delete(chatUser2);
                continue;
            }

            // at this point, we can be sure that user1 and user2 are present in chat.
            // now we have to change user2 to user1's lover if needed and if lover available as ChatMember
            boolean isTrueLove = false;
            var user1Stats = userStats.findById(chatUser1.getUserId());
            if (user1Stats.getLoverId() != null) {
                try {
                    user2 = getUserFromChatMember(chatId, user1Stats.getLoverId(), telegram);
                    isTrueLove = true;
                } catch (NoChatMemberException ignored) {
                    // leave as is
                }
            }

            return new PairData(user1, user2, isTrueLove);

        }
    }

    private void validateNameIsNotBlank(User user) throws BlankNameException {
        if (user.getFirstName().isBlank()) {
            throw new BlankNameException(user.getId());
        }
    }

    private User getUserFromChatMember(long chatId, long userId, CommonAbsSender telegram) {
        var member = Methods.getChatMember(chatId, userId).call(telegram);
        if (member == null || member.getStatus().equals("left") || member.getStatus().equals("kicked"))
            throw new NoChatMemberException(userId, chatId);
        return member.getUser();
    }

    private void sendRandomShitWithDelay(long chatId, String[] shit, CommonAbsSender telegram) {
        for (int i = 0; i < shit.length - 1; i++) {
            Methods.sendMessage(chatId, shit[i]).callAsync(telegram);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private record PairData(User first, User second, boolean isTrueLove) {

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

    private static class NotEnoughUsersException extends RuntimeException {
        public NotEnoughUsersException(int found, int required) {
            super("Not enough users, found %d, required %d".formatted(found, required));
        }
    }

    private static class NoChatMemberException extends RuntimeException {

        private final long userId;
        private final long chatId;

        public NoChatMemberException(long userId, long chatId) {
            super("No userId %d in chatId %d".formatted(userId, chatId));
            this.userId = userId;
            this.chatId = chatId;
        }

        public long getUserId() {
            return userId;
        }

        public long getChatId() {
            return chatId;
        }
    }

    private static class BlankNameException extends RuntimeException {

        public BlankNameException(long userId) {
            super("User with id " + userId + " has a blank name!");
        }

    }

}
