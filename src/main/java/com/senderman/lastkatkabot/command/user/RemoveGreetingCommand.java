package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RemoveGreetingCommand implements CommandExecutor {

    private final ChatInfoService chatInfoService;

    public RemoveGreetingCommand(ChatInfoService chatInfoService) {
        this.chatInfoService = chatInfoService;
    }

    @Override
    public String command() {
        return "/removegreeting";
    }

    @Override
    public String getDescription() {
        return "удалить стикер приветствия из бота";
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

        var chatSettings = chatInfoService.findById(chatId);
        var stickerId = chatSettings.getGreetingStickerId();

        if (stickerId == null) {
            ctx.replyToMessage("❌ У вас не задан приветственный стикер!").callAsync(ctx.sender);
            return;
        }

        chatSettings.setGreetingStickerId(null);
        chatInfoService.save(chatSettings);

        ctx.replyToMessage("✅ Стикер для приветствия успешно удалён!").callAsync(ctx.sender);
    }
}
