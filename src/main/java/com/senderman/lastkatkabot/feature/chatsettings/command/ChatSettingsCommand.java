package com.senderman.lastkatkabot.feature.chatsettings.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.ChatInfoService;
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
    public String getDescription() {
        return "настройки чата";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {

        var chatId = ctx.chatId();
        var sb = new StringBuilder("<b>Настройки чата</b>\n\n");
        sb.append("Запрещенные команды:\n");
        var chatInfo = chatInfoService.findById(chatId);


        var forbiddenCommands = Objects.requireNonNullElseGet(chatInfo.getForbiddenCommands(), HashSet::new);
        forbiddenCommands.forEach(c -> sb.append(c).append("\n"));
        sb.append("\n");

        ctx.reply(sb.toString()).callAsync(ctx.sender);
    }
}