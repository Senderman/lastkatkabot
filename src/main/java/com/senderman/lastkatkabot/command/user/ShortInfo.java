package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;

@Component
public class ShortInfo implements CommandExecutor {

    public ShortInfo() {
    }

    @Override
    public String command() {
        return "/shortinfo";
    }

    @Override
    public String getDescription() {
        return "краткая инфа о сообщении. Поддерживается реплай";
    }

    @Override
    public void accept(MessageContext ctx) {
        var chatId = ctx.chatId();
        var userId = ctx.user().getId();

        String info = String.format(
                " ==== Информация ====\n\n" +

                "💬 ID чата: <code>%d</code>\n" +
                "🙍‍♂️ Ваш ID: <code>%d</code>",
                chatId, userId);

        var message = ctx.message();
        if (message.isReply()) {
            var reply = message.getReplyToMessage();
            var replyMessageId = reply.getMessageId();
            var replyUserId = reply.getFrom().getId();
            info += String.format(
                    "\n\n" +
                    "✉️ ID reply: <code>%d</code>\n" +
                    "🙍‍♂ ID юзера из reply: <code>%d</code>",
                    replyMessageId, replyUserId);

            var forward = reply.getForwardFromChat();
            if (forward != null && forward.isChannelChat()) {
                info += String.format("\n\uD83D\uDCE2 ID канала: <code>%d</code>", forward.getId());
            }
        }
        ctx.reply(info).callAsync(ctx.sender);

    }
}
