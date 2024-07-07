package com.senderman.lastkatkabot.feature.tracking.command;

import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.TelegramUsersHelper;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Singleton
public class LinkToCommand implements CommandExecutor {

    private final TelegramUsersHelper users;

    public LinkToCommand(TelegramUsersHelper users) {
        this.users = users;
    }

    @Override
    public String command() {
        return "/linkto";
    }

    @Override
    public String getDescriptionKey() {
        return "linkto.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }
        try {
            long userId = Long.parseLong(ctx.argument(0));
            ctx.replyToMessage(Html.getUserLink(users.findUserFirstName(userId, ctx))).callAsync(ctx.sender);
        } catch (NumberFormatException e) {
            ctx.replyToMessage(ctx.getString("common.userIdIsNumber")).callAsync(ctx.sender);
        }
    }
}
