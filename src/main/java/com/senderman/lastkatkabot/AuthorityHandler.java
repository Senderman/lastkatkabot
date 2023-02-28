package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.access.model.AdminUser;
import com.senderman.lastkatkabot.feature.access.model.BlacklistedUser;
import com.senderman.lastkatkabot.feature.access.service.UserManager;
import com.senderman.lastkatkabot.feature.chatsettings.service.ChatInfoService;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.EnumSet;
import java.util.Locale;

@Singleton
public class AuthorityHandler implements Authority<Role> {

    private final UserManager<AdminUser> admins;
    private final UserManager<BlacklistedUser> blacklist;
    private final BotConfig botConfig;
    private final ChatInfoService chatInfoService;

    public AuthorityHandler(
            UserManager<AdminUser> admins,
            UserManager<BlacklistedUser> blacklist,
            BotConfig botConfig,
            ChatInfoService chatInfoService
    ) {
        this.admins = admins;
        this.blacklist = blacklist;
        this.botConfig = botConfig;
        this.chatInfoService = chatInfoService;
    }

    @Override
    public boolean hasRights(@NotNull CommonAbsSender sender, @NotNull Update update, @NotNull User user, @NotNull EnumSet<Role> roles) {
        var userId = user.getId();

        if (update.hasMessage()) {
            var message = update.getMessage();

            if (message.isCommand()) {
                if (!isCommandAllowed(message))
                    return false;
            }
        }

        // always allow main admin to execute commands
        if (userId.equals(botConfig.mainAdminId())) return true;

        if (blacklist.hasUser(userId)) return false;

        // allow command to be executed if it is user command
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
