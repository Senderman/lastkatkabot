package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class BadNeko implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final UserManager<BlacklistedUser> blackUsers;


    public BadNeko(
            CommonAbsSender telegram,
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
        if (!message.isReply() || message.isUserMessage()) {
            ApiRequests.answerMessage(message, "Опускать в плохие кисы нужно в группе и реплаем!")
                    .callAsync(telegram);
            return;
        }
        var user = message.getReplyToMessage().getFrom();
        var userLink = Html.getUserLink(user);
        if (user.getIsBot()) {
            ApiRequests.answerMessage(message, "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть плохой кисой?")
                    .callAsync(telegram);
            return;
        }

        if (blackUsers.addUser(new BlacklistedUser(user.getId())))
            ApiRequests.answerMessage(message, "Теперь " + userLink + " -  плохая киса!").callAsync(telegram);
        else
            ApiRequests.answerMessage(message, userLink + " уже плохая киса!").callAsync(telegram);

    }
}




