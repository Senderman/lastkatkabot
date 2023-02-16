package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.service.CurrentTime;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.Threads;
import jakarta.inject.Named;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Command
public class PairCommand implements CommandExecutor {

    private static final String EMPTY_NAME_REPLACEMENT = "Без имени";
    private final UserStatsService userStatsService;
    private final ChatUserService chatUsersService;
    private final ChatInfoService chatInfoService;
    private final List<String> love;
    private final CurrentTime currentTime;
    private final Set<Long> runningChatPairsGenerations;
    private final ExecutorService threadPool;

    public PairCommand(
            UserStatsService userStatsService,
            ChatUserService chatUsersService,
            ChatInfoService chatInfoService,
            List<String> love,
            CurrentTime currentTime,
            @Named("pairPool") ExecutorService threadPool
    ) {
        this.userStatsService = userStatsService;
        this.chatUsersService = chatUsersService;
        this.chatInfoService = chatInfoService;
        this.love = love;
        this.currentTime = currentTime;
        this.runningChatPairsGenerations = Collections.synchronizedSet(new HashSet<>());
        // I don't use pool from BotConfig, because it's already overloaded. /pair needs its own pool
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
        // ArrayList::new because we will use this list below
        List<String> lastPairs = Objects.requireNonNullElseGet(chatInfo.getLastPairs(), ArrayList::new);
        var lastPairGenerationDate = Objects.requireNonNullElse(chatInfo.getLastPairDate(), -1);
        int currentDay = Integer.parseInt(currentTime.getCurrentDay());

        // pair of today already exists
        if (!lastPairs.isEmpty() && lastPairGenerationDate == currentDay) {
            ctx.reply("Пара дня: " + lastPairs.get(0)).callAsync(ctx.sender);
            return;
        }

        if (!runningChatPairsGenerations.add(chatId)) return;

        chatUsersService.deleteInactiveChatUsers(chatId);

        final var usersForPair = chatUsersService.getTwoOrLessUsersOfChat(chatId);
        if (usersForPair.size() < 2) {
            ctx.reply("Недостаточно пользователей писало в чат за последние 2 недели!").callAsync(ctx.sender);
            runningChatPairsGenerations.remove(chatId);
            return;
        }

        // start chat flooding to make users wait for pair generation
        String[] loveStrings = love.get(ThreadLocalRandom.current().nextInt(love.size())).split("\n");
        Future<?> floodFuture = threadPool.submit(() -> sendRandomShitWithDelay(chatId, loveStrings, ctx.sender));

        threadPool.execute(() -> {
            try {
                PairData pair = generateNewPair(chatId, usersForPair);
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
                ctx.reply("...Упс, произошла ошибка. Попробуйте еще раз").callAsync(ctx.sender);
                throw new RuntimeException(e);
            } finally {
                runningChatPairsGenerations.remove(chatId);
            }
        });
    }


    // usersForPair should contain 2 or more users, otherwise this method will fail
    private PairData generateNewPair(long chatId, List<ChatUser> usersForPair) {
        var chatUser1 = usersForPair.get(0);
        var chatUser2 = usersForPair.get(1);
        var user1 = userFromChatUser(chatUser1);
        replaceNameIfBlank(user1);
        var user2 = userFromChatUser(chatUser2);
        replaceNameIfBlank(user2);

        // at this point, we can be sure that user1 and user2 are present in chat.
        // now we have to change user2 to user1's lover if needed and if lover is present in chat
        var user1Stats = userStatsService.findById(chatUser1.getUserId());
        if (user1Stats.getLoverId() == null)
            return new PairData(user1, user2, false);

        var loverOptional = chatUsersService.findByChatIdAndUserId(chatId, user1Stats.getLoverId());
        if (loverOptional.isEmpty()) {
            return new PairData(user1, user2, false);
        } else {
            // lover is found
            chatUser2 = loverOptional.get();
            user2 = userFromChatUser(chatUser2);
            replaceNameIfBlank(user2);
            return new PairData(user1, user2, true);
        }

    }

    private User userFromChatUser(ChatUser chatUser) {
        return new User(chatUser.getUserId(), chatUser.getName(), false);
    }

    private void replaceNameIfBlank(User user) {
        if (user.getFirstName().isBlank()) {
            user.setFirstName(EMPTY_NAME_REPLACEMENT);
        }
    }

    private void sendRandomShitWithDelay(long chatId, String[] shit, CommonAbsSender telegram) {
        for (int i = 0; i < shit.length - 1; i++) {
            Methods.sendMessage(chatId, shit[i]).callAsync(telegram);
            Threads.sleep(1000);
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

}
