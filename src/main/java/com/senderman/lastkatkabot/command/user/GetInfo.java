package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;

@Component
public class GetInfo implements CommandExecutor {

    public GetInfo() {
    }

    @Override
    public String getTrigger() {
        return "/getinfo";
    }

    @Override
    public String getDescription() {
        return "(reply) инфа о сообщении в формате JSON";
    }

    @Override
    public void execute(MessageContext ctx) {
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
