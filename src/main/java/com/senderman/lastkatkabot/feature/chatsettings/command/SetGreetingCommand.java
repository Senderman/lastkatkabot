package com.senderman.lastkatkabot.feature.chatsettings.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.ChatInfoService;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Singleton
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
        return "(reply на стикер) сделать стикер приветствием";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        long chatId = ctx.chatId();
        var userId = ctx.user().getId();
        var chatMember = Methods.getChatMember(chatId, userId).call(ctx.sender);
        if (chatMember == null || !Set.of("administrator", "creator").contains(chatMember.getStatus())) {
            ctx.replyToMessage("❌ Вы должны быть админом чата, чтобы использовать эту команду!").callAsync(ctx.sender);
            return;
        }

        var reply = ctx.message().getReplyToMessage();
        if (reply == null || !reply.hasSticker()) {
            ctx.replyToMessage("❌ Вы должны отправить эту команду на стикер, который станет приветствием!").callAsync(ctx.sender);
            return;
        }

        var stickerId = reply.getSticker().getFileId();
        var chatSettings = chatInfoService.findById(chatId);
        chatSettings.setGreetingStickerId(stickerId);
        chatInfoService.save(chatSettings);

        ctx.replyToMessage("✅ Стикер для приветствия успешно задан!").callAsync(ctx.sender);
    }
}
