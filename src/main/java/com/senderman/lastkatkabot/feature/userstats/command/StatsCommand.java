package com.senderman.lastkatkabot.feature.userstats.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.tracking.service.ChatUserService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.Optional;

@Command
public class StatsCommand implements CommandExecutor {

    private final UserStatsService users;
    private final ChatUserService chatUsers;


    public StatsCommand(UserStatsService users, ChatUserService chatUsers) {
        this.users = users;
        this.chatUsers = chatUsers;
    }

    @Override
    public String command() {
        return "/stats";
    }

    @Override
    public String getDescription() {
        return "userstats.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        User user = (ctx.message().isReply()) ? ctx.message().getReplyToMessage().getFrom() : ctx.user();

        if (user.getIsBot()) {
            ctx.replyToMessage(ctx.getString("userstats.isBot"))
                    .callAsync(ctx.sender);
            return;
        }

        var stats = users.findById(user.getId());
        String name = Html.htmlSafe(user.getFirstName());
        int winRate = stats.getDuelsTotal() == 0 ? 0 : 100 * stats.getDuelWins() / stats.getDuelsTotal();
        String text = ctx.getString("userstats.text")
                .formatted(name, stats.getDuelWins(), stats.getDuelsTotal(), winRate, stats.getBncScore(), stats.getLocale());
        var loverId = stats.getLoverId();
        if (loverId == null) {
            ctx.reply(text).callAsync(ctx.sender);
            return;
        }

        User lover = chatUsers.findNewestUserData(loverId)
                .map(l -> new User(l.getUserId(), l.getName(), false)) // get actual username from chatUsers table
                .or(() -> getUserDataFromTelegram(loverId, ctx.sender)) // fallback to request it from telegram
                .orElseGet(() -> new User(loverId, ctx.getString("common.unknownUser"), false)); // give up and set the name to "Unknown user"
        String loverLink = Html.getUserLink(lover);
        text += ctx.getString("userstats.lover").formatted(loverLink);
        ctx.reply(text).callAsync(ctx.sender);
    }

    private Optional<User> getUserDataFromTelegram(long userId, CommonAbsSender sender) {
        var member = Methods.getChatMember(userId, userId).call(sender);
        return Optional.ofNullable(member).map(ChatMember::getUser);
    }
}
