package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.util.Html;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Command(
        command = "/ulist",
        description = "показать списки пользователей (админы/чс)",
        authority = {Role.ADMIN, Role.MAIN_ADMIN}
)
public class ListUsersCommand extends CommandExecutor {

    private final UserManager<BlacklistedUser> blacklist;
    private final UserManager<AdminUser> admins;

    public ListUsersCommand(UserManager<BlacklistedUser> blacklist, UserManager<AdminUser> admins) {
        this.blacklist = blacklist;
        this.admins = admins;
    }

    @Override
    public void accept(MessageContext ctx) {

        String text = "<b>Списки пользователей</b>:\n\n" +
                "<b>Админы</b>\n\n" +
                formatUsers(admins) +
                "\n\n" +
                "<b>Плохие кисы</b>\n\n" +
                formatUsers(blacklist);
        ctx.replyToMessage(text).callAsync(ctx.sender);

    }

    private String formatUsers(UserManager<?> userManager) {
        return StreamSupport.stream(userManager.findAll().spliterator(), false)
                .map(u -> "<a href=\"tg://user?id=%d\">%s</a>".formatted(u.getId(), Html.htmlSafe(u.getName())))
                .collect(Collectors.joining("\n"));
    }
}
