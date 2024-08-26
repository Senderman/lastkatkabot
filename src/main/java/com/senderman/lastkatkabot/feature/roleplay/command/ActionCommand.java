package com.senderman.lastkatkabot.feature.roleplay.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import org.jetbrains.annotations.NotNull;

@Command
public class ActionCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/action";
    }

    @Override
    public String getDescriptionKey() {
        return "roleplay.action.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        ctx.deleteMessage().callAsync(ctx.sender);
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) return;

        var action = "<i>" + Html.htmlSafe(ctx.user().getFirstName()) + " " + ctx.argument(0) + "</i>";
        var sm = ctx.reply(action);
        if (ctx.message().isReply()) {
            var reply = ctx.message().getReplyToMessage();
            if (reply.getFrom().getIsBot()) {
                // it messes up with topics, so we just disabled this feature
                ctx.replyToMessage(ctx.getString("roleplay.action.notForBots")).callAsync(ctx.sender);
                return;
            }
            sm.inReplyTo(ctx.message().getReplyToMessage());
        }
        sm.callAsync(ctx.sender);
    }
}
