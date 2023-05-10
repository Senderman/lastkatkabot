package com.senderman.lastkatkabot.feature.tracking.command;

import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.access.model.AdminUser;
import com.senderman.lastkatkabot.feature.access.model.BlacklistedUser;
import com.senderman.lastkatkabot.feature.access.service.UserManager;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import com.senderman.lastkatkabot.util.Html;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Command
public class ListUsersCommand implements CommandExecutor {

    private final UserManager<BlacklistedUser> blacklist;
    private final UserManager<AdminUser> admins;

    public ListUsersCommand(UserManager<BlacklistedUser> blacklist, UserManager<AdminUser> admins) {
        this.blacklist = blacklist;
        this.admins = admins;
    }

    @Override
    public String command() {
        return "/ulist";
    }

    @Override
    public String getDescription() {
        return "tracking.ulist.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {

        String text = ctx.getString("tracking.ulist.listTitle") +
                ctx.getString("tracking.ulist.adminList") +
                formatUsers(admins) +
                "\n\n" +
                ctx.getString("tracking.ulist.bannedList") +
                formatUsers(blacklist);
        ctx.replyToMessage(text).callAsync(ctx.sender);

    }

    private String formatUsers(UserManager<?> userManager) {
        return StreamSupport.stream(userManager.findAll().spliterator(), false)
                .map(u -> "<a href=\"tg://user?id=%d\">%s</a>".formatted(u.getUserId(), Html.htmlSafe(u.getName())))
                .collect(Collectors.joining("\n"));
    }
}
