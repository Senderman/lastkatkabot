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
public class BadNekoCommand implements CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;

    public BadNekoCommand(@Named("blacklistManager") UserManager<BlacklistedUser> blackUsers) {
        this.blackUsers = blackUsers;
    }

    @Override
    public String command() {
        return "/badneko";
    }

    @Override
    public String getDescription() {
        return "access.badneko.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var message = ctx.message();
        if (!message.isReply() || message.isUserMessage()) {
            ctx.replyToMessage(ctx.getString("access.badneko.wrongUsage")).callAsync(ctx.sender);
            return;
        }
        var user = message.getReplyToMessage().getFrom();
        var userLink = Html.getUserLink(user);
        if (user.getIsBot()) {
            ctx.replyToMessage(ctx.getString("access.badneko.bot")).callAsync(ctx.sender);
            return;
        }

        if (blackUsers.addUser(new BlacklistedUser(user.getId(), user.getFirstName())))
            ctx.replyToMessage(ctx.getString("access.badneko.success").formatted(userLink)).callAsync(ctx.sender);
        else
            ctx.replyToMessage(ctx.getString("common.alreadyBadNeko").formatted(userLink)).callAsync(ctx.sender);

    }
}




