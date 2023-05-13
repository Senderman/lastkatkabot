package com.senderman.lastkatkabot.feature.chatsettings.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.ChatInfoService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Command
public class ResetGreetingCommand implements CommandExecutor {

    private final ChatInfoService chatInfoService;

    public ResetGreetingCommand(ChatInfoService chatInfoService) {
        this.chatInfoService = chatInfoService;
    }

    @Override
    public String command() {
        return "/resetgreeting";
    }

    @Override
    public String getDescription() {
        return "chatsettings.resetgreeting.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        long chatId = ctx.chatId();
        var userId = ctx.user().getId();
        var chatMember = Methods.getChatMember(chatId, userId).call(ctx.sender);
        if (chatMember == null || !Set.of("administrator", "creator").contains(chatMember.getStatus())) {
            ctx.replyToMessage(ctx.getString("common.mustBeChatAdmin")).callAsync(ctx.sender);
            return;
        }

        var chatSettings = chatInfoService.findById(chatId);
        var stickerId = chatSettings.getGreetingStickerId();

        if (stickerId == null) {
            ctx.replyToMessage(ctx.getString("chatsettings.resetgreeting.failure")).callAsync(ctx.sender);
            return;
        }

        chatSettings.setGreetingStickerId(null);
        chatInfoService.save(chatSettings);

        ctx.replyToMessage(ctx.getString("chatsettings.resetgreeting.success")).callAsync(ctx.sender);
    }
}
