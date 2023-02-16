package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import jakarta.inject.Singleton;

@Command
@Singleton
public class LastPairsCommand implements CommandExecutor {

    private final ChatInfoService chats;

    public LastPairsCommand(ChatInfoService chats) {
        this.chats = chats;
    }

    @Override
    public String command() {
        return "/lastpairs";
    }

    @Override
    public String getDescription() {
        return "последние 10 пар чата";
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
