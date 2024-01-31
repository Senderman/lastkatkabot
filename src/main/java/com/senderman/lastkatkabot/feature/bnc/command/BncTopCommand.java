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
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import com.senderman.lastkatkabot.util.callback.NoOpCallback;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
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

        top.append("\n").append(ctx.getString("bnc.bnctop.speedRunTop"));

        ctx.reply(top.toString()).setReplyMarkup(createKeyboard(ctx, bncRecords)).callAsync(ctx.sender);
    }

    private InlineKeyboardMarkup createKeyboard(L10nMessageContext ctx, List<BncRecord> bncRecords) {
        var builder = new MarkupBuilder();
        for (var descItem : ctx.getString("bnc.bnctop.speedRunDescription").split(",\\s*")) {
            builder.addButton(noOpButton(descItem));
        }
        builder.newRow();
        for (var r : bncRecords) {
            builder.addButton(noOpButton("%d %s".formatted(r.getLength(), r.isHexadecimal() ? "hex" : "dec")));
            builder.addButton(noOpButton(r.getName()));
            builder.addButton(noOpButton(timeUtils.formatTimeSpent(r.getTimeSpent())));
            builder.newRow();
        }
        return builder.build();
    }

    private InlineKeyboardButton noOpButton(String text) {
        return ButtonBuilder.callbackButton()
                .text(text)
                .payload(NoOpCallback.NAME)
                .create();
    }

    private String formatUser(long userId, int score, L10nMessageContext ctx) {
        Function<User, String> userPrinter =
                ctx.message().isUserMessage() ? Html::getUserLink : u -> Html.htmlSafe(u.getFirstName());
        String user = userPrinter.apply(usersHelper.findUserFirstName(userId, ctx));

        return "%s (%d)".formatted(user, score);
    }
}
