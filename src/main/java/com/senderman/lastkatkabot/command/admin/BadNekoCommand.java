package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Named;

@Command(
        command = "/badneko",
        description = "опущение до плохой кисы. реплаем.",
        authority = {Role.ADMIN, Role.MAIN_ADMIN}
)
public class BadNekoCommand extends CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;

    public BadNekoCommand(
            @Named("blacklistManager") UserManager<BlacklistedUser> blackUsers
    ) {
        this.blackUsers = blackUsers;
    }

    @Override
    public void accept(MessageContext ctx) {
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




