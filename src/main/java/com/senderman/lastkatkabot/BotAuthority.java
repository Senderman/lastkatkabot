package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.service.ChatPolicyEnsuringService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.EnumSet;
import java.util.Set;

@Component
public class BotAuthority implements Authority<Role> {

    private final UserManager<AdminUser> admins;
    private final UserManager<BlacklistedUser> blacklist;
    private final BotConfig botConfig;
    private final Set<Long> telegramServiceUserIds;
    private final ChatPolicyEnsuringService chatPolicy;

    public BotAuthority(
            UserManager<AdminUser> admins,
            UserManager<BlacklistedUser> blacklist,
            BotConfig botConfig,
            ChatPolicyEnsuringService chatPolicy
    ) {
        this.admins = admins;
        this.blacklist = blacklist;
        this.botConfig = botConfig;
        this.chatPolicy = chatPolicy;
        this.telegramServiceUserIds = Set.of(
                777000L, // attached channel's messages
                1087968824L, // anonymous group admin @GroupAnonymousBot
                136817688L // Channel message, @Channel_Bot
        );
    }

    @Override
    public boolean hasRights(Update update, User user, EnumSet<Role> roles) {
        var userId = user.getId();

        if (update.hasMessage() && !checkMessage(update.getMessage())) return false;

        if (telegramServiceUserIds.contains(userId)) return false;

        if (userId.equals(botConfig.mainAdminId())) return true;

        if (blacklist.hasUser(userId)) return false;

        if (roles.contains(Role.USER)) return true;

        return roles.contains(Role.ADMIN) && admins.hasUser(userId);
    }

    private boolean checkMessage(Message message) {
        chatPolicy.queueViolationCheck(message.getChatId());
        // do not process messages older than 2 minutes
        return message.getDate() + 120 >= System.currentTimeMillis() / 1000;
    }
}
