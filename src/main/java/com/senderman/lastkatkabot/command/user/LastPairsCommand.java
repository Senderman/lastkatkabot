package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;

@Command(
        command = "/lastpairs",
        description = "последние 10 пар чата"
)
public class LastPairsCommand extends CommandExecutor {

    private final ChatInfoService chats;

    public LastPairsCommand(ChatInfoService chats) {
        this.chats = chats;
    }

    @Override
    public void accept(MessageContext ctx) {
        if (ctx.message().isUserMessage()) {
            ctx.replyToMessage("Команду нельзя использовать в ЛС!").callAsync(ctx.sender);
            return;
        }

        var chatInfo = chats.findById(ctx.chatId());
        var pairs = chatInfo.getLastPairs();
        if (pairs == null || pairs.isEmpty()) {
            ctx.replyToMessage("В этом чате еще ни разу не запускали /pair!").callAsync(ctx.sender);
            return;
        }

        var text = "<b>Последние 10 пар:</b>\n\n" + String.join("\n", pairs);
        ctx.reply(text).callAsync(ctx.sender);
    }
}
