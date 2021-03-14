package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class BadNeko implements CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;


    public BadNeko(
            @Qualifier("blacklistManager") UserManager<BlacklistedUser> blackUsers
    ) {
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
    public void execute(MessageContext ctx) {
        var message = ctx.message();
        if (!message.isReply() || message.isUserMessage()) {
            ctx.replyToMessage("Опускать в плохие кисы нужно в группе и реплаем!").callAsync(ctx.sender);
            return;
        }
        var user = message.getReplyToMessage().getFrom();
        var userLink = Html.getUserLink(user);
        if (user.getIsBot()) {
            ctx.replyToMessage(
                    "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть плохой кисой?")
                    .callAsync(ctx.sender);
            return;
        }

        if (blackUsers.addUser(new BlacklistedUser(user.getId(), user.getFirstName())))
            ctx.replyToMessage("Теперь " + userLink + " -  плохая киса!").callAsync(ctx.sender);
        else
            ctx.replyToMessage(userLink + " уже плохая киса!").callAsync(ctx.sender);

    }
}




