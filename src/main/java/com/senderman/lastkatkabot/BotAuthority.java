package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.EnumSet;

@Component
public class BotAuthority implements Authority<Role> {

    private final UserManager<AdminUser> admins;
    private final UserManager<BlacklistedUser> blacklist;
    private final BotConfig botConfig;

    public BotAuthority(UserManager<AdminUser> admins, UserManager<BlacklistedUser> blacklist, BotConfig botConfig) {
        this.admins = admins;
        this.blacklist = blacklist;
        this.botConfig = botConfig;
    }

    @Override
    public boolean hasRights(Update update, User user, EnumSet<Role> roles) {
        var userId = user.getId();
        if (userId.equals(botConfig.mainAdminId())) return true;

        if (blacklist.hasUser(userId)) return false;

        if (roles.contains(Role.USER)) return true;

        return roles.contains(Role.ADMIN) && admins.hasUser(userId);
    }
}
