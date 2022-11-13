package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;

@Command(
        command = "/getinfo",
        description = "(reply) инфа о сообщении в формате JSON"
)
public class GetInfoCommand extends CommandExecutor {

    public GetInfoCommand() {
    }

    @Override
    public void accept(MessageContext ctx) {
        var message = ctx.message();
        if (!message.isReply()) {
            ctx.replyToMessage("Для использования команды, отправьте ее в ответ на нужное сообщение!").callAsync(ctx.sender);
            return;
        }

        var replyInfo = message.getReplyToMessage()
                .toString()
                .replaceAll("\\w+=null,?\\s*", "")
                .replaceAll("=(\\w+)", "=<code>$1</code>");

        ctx.reply(replyInfo).callAsync(ctx.sender);
    }
}
