package com.senderman.lastkatkabot.feature.tracking.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import jakarta.inject.Singleton;

@Singleton
@Command
public class ShortInfoCommand implements CommandExecutor {

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

        String info = """
                ==== Информация ====

                💬 ID чата: <code>%d</code>
                🙍‍♂️ Ваш ID: <code>%d</code>"""
                .formatted(chatId, userId);

        var message = ctx.message();
        if (message.isReply()) {
            var reply = message.getReplyToMessage();
            var replyMessageId = reply.getMessageId();
            var replyUserId = reply.getFrom().getId();
            info += """


                    ✉️ ID reply: <code>%d</code>
                    🙍‍♂ ID юзера из reply: <code>%d</code>"""
                    .formatted(replyMessageId, replyUserId);

            var forward = reply.getForwardFromChat();
            if (forward != null && forward.isChannelChat()) {
                info += "\n\uD83D\uDCE2 ID канала: <code>%d</code>".formatted(forward.getId());
            }
        }
        ctx.reply(info).callAsync(ctx.sender);

    }
}
