package com.senderman.lastkatkabot.feature.chatsettings.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.ChatInfoService;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Command
public class SetGreetingCommand implements CommandExecutor {

    private final ChatInfoService chatInfoService;

    public SetGreetingCommand(ChatInfoService chatInfoService) {
        this.chatInfoService = chatInfoService;
    }

    @Override
    public String command() {
        return "/greeting";
    }

    @Override
    public String getDescription() {
        return "chatsettings.greeting.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        long chatId = ctx.chatId();
        var userId = ctx.user().getId();
        var chatMember = Methods.getChatMember(chatId, userId).call(ctx.sender);
        if (chatMember == null || !Set.of("administrator", "creator").contains(chatMember.getStatus())) {
            ctx.replyToMessage(ctx.getString("common.mustBeChatAdmin")).callAsync(ctx.sender);
            return;
        }

        var reply = ctx.message().getReplyToMessage();
        if (reply == null || !reply.hasSticker()) {
            ctx.replyToMessage(ctx.getString("chatsettings.greeting.wrongUsage")).callAsync(ctx.sender);
            return;
        }

        var stickerId = reply.getSticker().getFileId();
        var chatSettings = chatInfoService.findById(chatId);
        chatSettings.setGreetingStickerId(stickerId);
        chatInfoService.save(chatSettings);

        ctx.replyToMessage(ctx.getString("chatsettings.greeting.success")).callAsync(ctx.sender);
    }
}
