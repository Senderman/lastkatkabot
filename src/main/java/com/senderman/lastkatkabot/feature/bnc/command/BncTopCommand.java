package com.senderman.lastkatkabot.feature.bnc.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.Optional;
import java.util.function.Function;

@Command
public class BncTopCommand implements CommandExecutor {

    private final UserStatsService users;

    public BncTopCommand(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return "/bnctop";
    }

    @Override
    public String getDescription() {
        return "bnc.bnctop.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {

        boolean chatTop = ctx.argument(0, "").equals("chat");
        if (chatTop && ctx.message().isUserMessage()) {
            ctx.replyToMessage(ctx.getString("bnc.bnctop.wrongUsage")).callAsync(ctx.sender);
            return;
        }
        String title = (
                chatTop ? ctx.getString("bnc.bnctop.chatTop") : ctx.getString("bnc.bnctop.top")
        ) + "\n\n";
        var topUsers = chatTop ? users.findTop10BncPlayersByChat(ctx.chatId()) : users.findTop10BncPlayers();
        if (topUsers.isEmpty()) {
            ctx.replyToMessage(ctx.getString("bnc.bnctop.emptyList")).callAsync(ctx.sender);
            return;
        }
        int counter = 0;
        var top = new StringBuilder(title);
        for (var user : topUsers) {
            top.append(++counter)
                    .append(": ")
                    .append(formatUser(user.getUserId(), user.getBncScore(), ctx))
                    .append("\n");
        }

        ctx.reply(top.toString()).callAsync(ctx.sender);
    }

    private String formatUser(long userId, int score, L10nMessageContext ctx) {
        Function<User, String> userPrinter =
                ctx.message().isUserMessage() ? Html::getUserLink : u -> Html.htmlSafe(u.getFirstName());
        String user = Optional.ofNullable(Methods.getChatMember(userId, userId).call(ctx.sender))
                .map(ChatMember::getUser)
                .map(userPrinter)
                .orElse(ctx.getString("common.noName"));

        return user + " (" + score + ")";
    }
}
