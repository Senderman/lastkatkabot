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
        command = "/goodneko",
        description = "повышение до хорошей кисы. реплаем.",
        authority = {Role.ADMIN, Role.MAIN_ADMIN}
)
public class GoodNekoCommand extends CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;

    public GoodNekoCommand(
            @Named("blacklistManager") UserManager<BlacklistedUser> blackUsers
    ) {
        this.blackUsers = blackUsers;
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




