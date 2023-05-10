package com.senderman.lastkatkabot.feature.love.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.ChatInfoService;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import org.jetbrains.annotations.NotNull;

@Command
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
        return "love.lastpairs.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        if (ctx.message().isUserMessage()) {
            ctx.replyToMessage("love.lastpairs.wrongUsage").callAsync(ctx.sender);
            return;
        }

        var chatInfo = chats.findById(ctx.chatId());
        var pairs = chatInfo.getLastPairs();
        if (pairs == null || pairs.isEmpty()) {
            ctx.replyToMessage(ctx.getString("love.lastpairs.emptyList")).callAsync(ctx.sender);
            return;
        }

        var text = ctx.getString("love.lastpairs.listTitle") + String.join("\n", pairs);
        ctx.reply(text).callAsync(ctx.sender);
    }
}
