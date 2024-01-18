package com.senderman.lastkatkabot.feature.tracking.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;

@Command
public class WhoInChatCommand implements CommandExecutor {

    private final UserStatsService userStats;
    private final ExecutorService threadPool;

    public WhoInChatCommand(UserStatsService userStats, @Named("generalNeedsPool") ExecutorService threadPool) {
        this.userStats = userStats;
        this.threadPool = threadPool;
    }

    @Override
    public String command() {
        return "/wic";
    }

    @Override
    public String getDescription() {
        return "tracking.wic.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("tracking.wic.wrongUsage")).callAsync(ctx.sender);
            return;
        }
        long chatId;
        try {
            chatId = Long.parseLong(ctx.argument(0));
        } catch (NumberFormatException e) {
            ctx.replyToMessage(ctx.getString("common.chatIdIsNumber")).callAsync(ctx.sender);
            return;
        }

        threadPool.execute(() -> {
            var users = userStats.findByChatId(chatId)
                    .stream()
                    .map(this::formatUser)
                    .toList();

            if (users.isEmpty()) {
                ctx.replyToMessage(ctx.getString("tracking.wic.chatIsEmpty")).callAsync(ctx.sender);
                return;
            }

            var text = new StringBuilder(ctx.getString("tracking.wic.usersFound")
                    .formatted(getChatNameOrChatId(chatId, ctx.sender)));
            for (var user : users) {
                if (text.length() + "\n".length() + user.length() >= 4096) {
                    ctx.replyToMessage(text.toString()).callAsync(ctx.sender);
                    text.setLength(0);
                }
                text.append(user).append("\n");
            }
            // send remaining users
            if (!text.isEmpty()) {
                ctx.replyToMessage(text.toString()).callAsync(ctx.sender);
            }
        });

    }

    private String formatUser(UserStats user) {
        return "%s (<code>%d</code>)".formatted(Html.getUserLink(user.getUserId(), user.getName()), user.getUserId());
    }

    // get chat name. If unable to get if from tg, return chatId as string
    private String getChatNameOrChatId(long chatId, CommonAbsSender telegram) {
        var chat = Methods.getChat(chatId).call(telegram);
        return chat != null ? Html.htmlSafe(chat.getTitle()) + " (<code>%d</code>)".formatted(chatId) : String.valueOf(chatId);
    }
}
