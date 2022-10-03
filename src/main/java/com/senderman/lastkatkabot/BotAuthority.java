package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.service.ChatPolicyEnsuringService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

@Component
public class BotAuthority implements Authority<Role> {

    private final UserManager<AdminUser> admins;
    private final UserManager<BlacklistedUser> blacklist;
    private final BotConfig botConfig;
    private final ExecutorService threadPool;

    private final ChatPolicyEnsuringService chatPolicy;
    private final ChatInfoService chatInfoService;

    public BotAuthority(
            UserManager<AdminUser> admins,
            UserManager<BlacklistedUser> blacklist,
            BotConfig botConfig,
            ChatPolicyEnsuringService chatPolicy,
            @Qualifier("generalNeedsPool") ExecutorService threadPool,
            ChatInfoService chatInfoService) {
        this.admins = admins;
        this.blacklist = blacklist;
        this.botConfig = botConfig;
        this.chatPolicy = chatPolicy;
        this.threadPool = threadPool;
        this.chatInfoService = chatInfoService;
    }

    @Override
    public boolean hasRights(Update update, User user, EnumSet<Role> roles) {
        var userId = user.getId();

        if (update.hasMessage()) {
            var message = update.getMessage();
            threadPool.execute(() -> chatPolicy.queueViolationCheck(message.getChatId()));
            // do not process messages older than 2 minutes
            if (message.getDate() + 120 < System.currentTimeMillis() / 1000)
                return true;
            if (message.isCommand()) {
                if (!isCommandAllowed(message))
                    return false;
            }
        }

        if (userId.equals(botConfig.mainAdminId())) return true;

        if (blacklist.hasUser(userId)) return false;

        if (roles.contains(Role.USER)) return true;

        return roles.contains(Role.ADMIN) && admins.hasUser(userId);
    }

    private boolean isCommandAllowed(Message message) {
        var chatInfoObject = chatInfoService.findById(message.getChatId());
        var forbiddenCommands = chatInfoObject.getForbiddenCommands();
        if (forbiddenCommands == null)
            return true;

        String command = message.getText()
                .split("\\s+", 2)[0]
                .toLowerCase(Locale.ENGLISH)
                .replace("@" + botConfig.username(), "");

        return !forbiddenCommands.contains(command);
    }
}
