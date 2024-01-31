package com.senderman.lastkatkabot.feature.bnc.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.bnc.model.BncRecord;
import com.senderman.lastkatkabot.feature.bnc.service.BncRecordService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.TelegramUsersHelper;
import com.senderman.lastkatkabot.util.TimeUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.function.Function;

@Command
public class BncTopCommand implements CommandExecutor {

    private final UserStatsService users;
    private final TelegramUsersHelper usersHelper;
    private final BncRecordService bncRecordsRepo;
    private final TimeUtils timeUtils;

    public BncTopCommand(
            UserStatsService users,
            TelegramUsersHelper usersHelper,
            BncRecordService bncRecordsRepo,
            TimeUtils timeUtils
    ) {
        this.users = users;
        this.usersHelper = usersHelper;
        this.bncRecordsRepo = bncRecordsRepo;
        this.timeUtils = timeUtils;
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
        // do not send speedrun top if requested local group stats
        if (chatTop) {
            ctx.reply(top.toString()).callAsync(ctx.sender);
            return;
        }
        var bncRecords = bncRecordsRepo.findAllOrderByHexadecimalAndLength();
        if (bncRecords.isEmpty()) {
            ctx.reply(top.toString()).callAsync(ctx.sender);
            return;
        }

        top.append("\n")
                .append(ctx.getString("bnc.bnctop.speedRunTop"))
                .append("\n")
                .append(ctx.getString("bnc.bnctop.speedRunDescription"))
                .append("\n\n");

        for (var r : bncRecords) {
            top.append(formatRecord(r)).append("\n");
        }

        ctx.reply(top.toString()).callAsync(ctx.sender);
    }

    private String formatRecord(BncRecord r) {
        return "<code>%-2d %s</code> %s: %s".formatted(
                r.getLength(),
                r.isHexadecimal() ? "hex" : "dec",
                Html.getUserLink(new User(r.getUserId(), r.getName(), false)),
                timeUtils.formatTimeSpent(r.getTimeSpent())
        );
    }

    private String formatUser(long userId, int score, L10nMessageContext ctx) {
        Function<User, String> userPrinter =
                ctx.message().isUserMessage() ? Html::getUserLink : u -> Html.htmlSafe(u.getFirstName());
        String user = userPrinter.apply(usersHelper.findUserFirstName(userId, ctx));

        return "%s (%d)".formatted(user, score);
    }
}
