package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.service.UserManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class BadNeko implements CommandExecutor {

    private final ApiRequests telegram;
    private final UserManager<BlacklistedUser> blackUsers;


    public BadNeko(
            ApiRequests telegram,
            @Qualifier("blacklistManager") UserManager<BlacklistedUser> blackUsers
    ) {
        this.telegram = telegram;
        this.blackUsers = blackUsers;
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public String getTrigger() {
        return "/badneko";
    }

    @Override
    public String getDescription() {
        return "опущение до плохой кисы. реплаем.";
    }

    @Override
    public void execute(Message message) {
        long chatId = message.getChatId();
        var messageId = message.getMessageId();
        if (!message.isReply() || message.isUserMessage()) {
            telegram.sendMessage(chatId, "Опускать в плохие кисы нужно в группе и реплаем!");
            return;
        }
        var user = message.getReplyToMessage().getFrom();

        if (user.getIsBot()) {
            telegram.sendMessage(chatId, "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть плохой кисой?");
            return;
        }

        if (blackUsers.addUser(new BlacklistedUser(user.getId())))
            telegram.sendMessage(chatId, "Теперь он плохая киса!", messageId);
        else
            telegram.sendMessage(chatId, "Он уже плохая киса!", messageId);

    }
}




