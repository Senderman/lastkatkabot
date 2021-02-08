package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.service.UserManager;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class GoodNeko implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final UserManager<BlacklistedUser> blackUsers;

    public GoodNeko(
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
        return "/goodneko";
    }

    @Override
    public String getDescription() {
        return "повышение до хорошей кисы. реплаем.";
    }

    @Override
    public void execute(Message message) {
        if (!message.isReply() || message.isUserMessage()) {
            ApiRequests.answerMessage(message, "Возвышать до хороших нужно в группе и реплаем!")
                    .callAsync(telegram);
            return;
        }
        var user = message.getReplyToMessage().getFrom();
        var userLink = Html.getUserLink(user);
        if (user.getIsBot()) {
            ApiRequests.answerMessage(message, "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть хорошей кисой?")
                    .callAsync(telegram);
            return;
        }
        if (blackUsers.deleteUser(new BlacklistedUser(user.getId())))
            ApiRequests.answerMessage(message, "Теперь " + userLink + " -  хорошая киса!").callAsync(telegram);
        else
            ApiRequests.answerMessage(message, userLink + " уже хорошая киса!").callAsync(telegram);

    }
}




