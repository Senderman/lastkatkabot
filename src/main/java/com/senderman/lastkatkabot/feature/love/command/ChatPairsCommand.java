package com.senderman.lastkatkabot.feature.love.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.tracking.model.ChatUser;
import com.senderman.lastkatkabot.feature.tracking.service.ChatUserService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Command
public class ChatPairsCommand implements CommandExecutor {

    private final ChatUserService chatUsers;
    private final ExecutorService threadPool;
    private final UserStatsService userStats;

    public ChatPairsCommand(ChatUserService chatUsers, @Named("generalNeedsPool") ExecutorService threadPool, UserStatsService userStats) {
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
        this.userStats = userStats;
    }

    @Override
    public String command() {
        return "/chatpairs";
    }

    @Override
    public String getDescription() {
        return "love.chatpairs.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        threadPool.execute(() -> execute(ctx));
    }

    private void execute(L10nMessageContext ctx) {
        if (ctx.message().isUserMessage()) {
            ctx.replyToMessage(ctx.getString("common.noUsageInPM")).callAsync(ctx.sender);
            return;
        }

        var text = new StringBuilder(ctx.getString("love.chatpairs.loverList"));
        var users = chatUsers.findByChatId(ctx.chatId())
                .stream()
                .collect(Collectors.toMap(ChatUser::getUserId, ChatUser::getName));
        var lovers = userStats.findByIdAndLoverIdIn(users.keySet());
        if (lovers.isEmpty()) {
            ctx.reply(ctx.getString("love.chatpairs.loversEmpty")).callAsync(ctx.sender);
            return;
        }
        var processedUsers = new HashSet<Long>();
        for (var loverStat : lovers) {
            if (!processedUsers.add(loverStat.getUserId())) continue;
            processedUsers.add(loverStat.getLoverId());
            text.append("%s \uD83D\uDC9E %s\n".formatted(users.get(loverStat.getLoverId()), users.get(loverStat.getUserId())));
        }

        ctx.reply(text.toString()).callAsync(ctx.sender);
    }
}
