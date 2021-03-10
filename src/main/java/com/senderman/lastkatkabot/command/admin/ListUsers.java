package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

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
    public void execute(Message message, CommonAbsSender telegram) {

        String sb = "<b>Списки пользователей:\n\n" +
                    "<b>Админы</b>\n\n" +
                    formatUsers(admins) +
                    "\n\n" +
                    "<b>Плохие кисы</b>\n\n" +
                    formatUsers(blacklist);
        ApiRequests.answerMessage(message, sb).callAsync(telegram);

    }

    private String formatUsers(UserManager<?> userManager) {
        return StreamSupport.stream(userManager.findAll().spliterator(), false)
                .map(u -> "<a href=\"tg://user?id=%d\">%s</a>".formatted(u.getId(), Html.htmlSafe(u.getName())))
                .collect(Collectors.joining("\n"));
    }
}
