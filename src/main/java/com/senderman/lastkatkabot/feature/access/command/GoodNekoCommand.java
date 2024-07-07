package com.senderman.lastkatkabot.feature.access.command;

import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.access.model.BlacklistedUser;
import com.senderman.lastkatkabot.feature.access.service.UserManager;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Command
public class GoodNekoCommand implements CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;

    public GoodNekoCommand(@Named("blacklistManager") UserManager<BlacklistedUser> blackUsers) {
        this.blackUsers = blackUsers;
    }

    @Override
    public String command() {
        return "/goodneko";
    }

    @Override
    public String getDescriptionKey() {
        return "access.goodneko.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage()) {
            ctx.replyToMessage(ctx.getString("access.goodneko.wrongUsage")).callAsync(ctx.sender);
            return;
        }
        var user = ctx.message().getReplyToMessage().getFrom();
        if (user.getIsBot()) {
            ctx.replyToMessage(ctx.getString("access.goodneko.bot")).callAsync(ctx.sender);
            return;
        }
        var userLink = Html.getUserLink(user);
        if (blackUsers.deleteById(user.getId()))
            ctx.replyToMessage(ctx.getString("access.goodneko.success").formatted(userLink)).callAsync(ctx.sender);
        else
            ctx.replyToMessage(ctx.getString("access.goodneko.failure").formatted(userLink)).callAsync(ctx.sender);

    }
}




