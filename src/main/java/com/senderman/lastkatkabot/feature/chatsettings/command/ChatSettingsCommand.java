package com.senderman.lastkatkabot.feature.chatsettings.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.ChatInfoService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;

@Command
public class ChatSettingsCommand implements CommandExecutor {

    private final ChatInfoService chatInfoService;

    public ChatSettingsCommand(ChatInfoService chatInfoService) {
        this.chatInfoService = chatInfoService;
    }

    @Override
    public String command() {
        return "/settings";
    }

    @Override
    public String getDescriptionKey() {
        return "chatsettings.settings.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {

        var chatId = ctx.chatId();
        var sb = new StringBuilder(ctx.getString("chatsettings.settings.chatSettings") + "\n\n");
        sb.append(ctx.getString("chatsettings.settings.forbiddenCommands")).append("\n");
        var chatInfo = chatInfoService.findById(chatId);


        var forbiddenCommands = Objects.requireNonNullElseGet(chatInfo.getForbiddenCommands(), HashSet::new);
        forbiddenCommands.forEach(c -> sb.append(c).append("\n"));
        sb.append("\n");

        ctx.reply(sb.toString()).callAsync(ctx.sender);
    }
}
