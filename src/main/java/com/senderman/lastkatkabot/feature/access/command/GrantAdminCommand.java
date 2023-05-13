package com.senderman.lastkatkabot.feature.access.command;

import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.access.model.AdminUser;
import com.senderman.lastkatkabot.feature.access.service.UserManager;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Command
public class GrantAdminCommand implements CommandExecutor {

    private final UserManager<AdminUser> admins;

    public GrantAdminCommand(@Named("adminManager") UserManager<AdminUser> admins) {
        this.admins = admins;
    }

    @Override
    public String command() {
        return "/grantadmin";
    }

    @Override
    public String getDescription() {
        return "access.grantadmin.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage()) {
            ctx.replyToMessage(ctx.getString("access.grantadmin.wrongUsage")).callAsync(ctx.sender);
            return;
        }
        var user = ctx.message().getReplyToMessage().getFrom();

        if (user.getIsBot()) {
            ctx.replyToMessage(ctx.getString("access.grantadmin.bot")).callAsync(ctx.sender);
            return;
        }

        if (admins.addUser(new AdminUser(user.getId(), user.getFirstName())))
            ctx.replyToMessage(ctx.getString("access.grantadmin.success")).callAsync(ctx.sender);
        else
            ctx.replyToMessage(ctx.getString("access.grantadmin.failure")).callAsync(ctx.sender);
    }
}




