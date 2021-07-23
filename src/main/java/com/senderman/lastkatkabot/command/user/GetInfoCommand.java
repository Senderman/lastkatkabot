package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;

@Component
public class GetInfoCommand implements CommandExecutor {

    public GetInfoCommand() {
    }

    @Override
    public String command() {
        return "/getinfo";
    }

    @Override
    public String getDescription() {
        return "(reply) инфа о сообщении в формате JSON";
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
                .replaceAll("=([\\w\\d]+)", "=<code>$1</code>");

        ctx.reply(replyInfo).callAsync(ctx.sender);
    }
}
