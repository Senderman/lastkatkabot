package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ListUsers implements CommandExecutor {

    private final UserManager<BlacklistedUser> blacklist;
    private final UserManager<AdminUser> admins;

    public ListUsers(UserManager<BlacklistedUser> blacklist, UserManager<AdminUser> admins) {
        this.blacklist = blacklist;
        this.admins = admins;
    }

    @Override
    public String getTrigger() {
        return "/ulist";
    }

    @Override
    public String getDescription() {
        return "показать списки пользователей (админы/чс)";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.MAIN_ADMIN, Role.ADMIN);
    }

    @Override
    public void execute(MessageContext ctx) {

        String sb = "<b>Списки пользователей</b>:\n\n" +
                    "<b>Админы</b>\n\n" +
                    formatUsers(admins) +
                    "\n\n" +
                    "<b>Плохие кисы</b>\n\n" +
                    formatUsers(blacklist);
        ctx.replyToMessage(sb.toString()).callAsync(ctx.sender);

    }

    private String formatUsers(UserManager<?> userManager) {
        return StreamSupport.stream(userManager.findAll().spliterator(), false)
                .map(u -> "<a href=\"tg://user?id=%d\">%s</a>".formatted(u.getId(), Html.htmlSafe(u.getName())))
                .collect(Collectors.joining("\n"));
    }
}
