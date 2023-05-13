package com.senderman.lastkatkabot.feature.tracking.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

@Command
public class ShortInfoCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/shortinfo";
    }

    @Override
    public String getDescription() {
        return "tracking.shortinfo.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var chatId = ctx.chatId();
        var userId = ctx.user().getId();

        String info = ctx.getString("tracking.shortinfo.text").formatted(chatId, userId);

        var message = ctx.message();
        if (message.isReply()) {
            var reply = message.getReplyToMessage();
            var replyMessageId = reply.getMessageId();
            var replyUserId = reply.getFrom().getId();
            info += ctx.getString("tracking.shortinfo.replyText").formatted(replyMessageId, replyUserId);

            var forward = reply.getForwardFromChat();
            if (forward != null && forward.isChannelChat()) {
                info += ctx.getString("tracking.shortinfo.forwardText").formatted(forward.getId());
            }
        }
        ctx.reply(info).callAsync(ctx.sender);

    }
}
