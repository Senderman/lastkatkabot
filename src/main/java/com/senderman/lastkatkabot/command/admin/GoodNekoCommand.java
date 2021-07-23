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
public class GoodNekoCommand implements CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;

    public GoodNekoCommand(
            @Qualifier("blacklistManager") UserManager<BlacklistedUser> blackUsers
    ) {
        this.blackUsers = blackUsers;
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public String command() {
        return "/goodneko";
    }

    @Override
    public String getDescription() {
        return "повышение до хорошей кисы. реплаем.";
    }

    @Override
    public void accept(MessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage()) {
            ctx.replyToMessage("Позвышать до хороших кис нужно в группе и реплаем!").callAsync(ctx.sender);
            return;
        }
        var user = ctx.message().getReplyToMessage().getFrom();
        if (user.getIsBot()) {
            ctx.replyToMessage(
                    "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть хорошей кисой?")
                    .callAsync(ctx.sender);
            return;
        }
        var userLink = Html.getUserLink(user);
        if (blackUsers.deleteById(user.getId()))
            ctx.replyToMessage("Теперь " + userLink + " -  хорошая киса!").callAsync(ctx.sender);
        else
            ctx.replyToMessage(userLink + " уже хорошая киса!").callAsync(ctx.sender);

    }
}




