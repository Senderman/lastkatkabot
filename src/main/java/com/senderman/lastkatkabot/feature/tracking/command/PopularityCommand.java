package com.senderman.lastkatkabot.feature.tracking.command;

import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;

@Command
public class PopularityCommand implements CommandExecutor {

    private final UserStatsService userStats;
    private final ExecutorService threadPool;

    public PopularityCommand(@Named("generalNeedsPool") ExecutorService threadPool, UserStatsService userStats) {
        this.threadPool = threadPool;
        this.userStats = userStats;
    }

    @Override
    public String command() {
        return "/popularity";
    }

    @Override
    public String getDescriptionKey() {
        return "tracking.popularity.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        threadPool.execute(() -> {
            var text = ctx.getString("tracking.popularity.title");
            var chatsWithUsers = userStats.getTotalUniqueGroups();
            text += ctx.getString("tracking.popularity.activeChats").formatted(chatsWithUsers);
            var users = userStats.getTotalUniqueUsers();
            text += ctx.getString("tracking.popularity.activeUsers").formatted(users);
            ctx.reply(text).callAsync(ctx.sender);
        });
    }
}
