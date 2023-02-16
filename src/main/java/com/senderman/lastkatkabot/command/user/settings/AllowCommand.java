package com.senderman.lastkatkabot.command.user.settings;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.AccessManagerCommand;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.service.CommandAccessManager;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

@Singleton
@AccessManagerCommand
public class AllowCommand implements CommandExecutor {

    private final CommandAccessManager commandAccessManager;

    public AllowCommand(CommandAccessManager commandAccessManager) {
        this.commandAccessManager = commandAccessManager;
    }

    @Override
    public String command() {
        return "/callow";
    }

    @Override
    public String getDescription() {
        return "разрешить команды. Использование: /callow /command1 command2";
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
        if (ctx.argumentsLength() == 0) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }

        try {
            commandAccessManager.allowCommands(chatId, Arrays.asList(ctx.arguments()));
            ctx.replyToMessage("✅ Указанные команды успешно разрешены!").callAsync(ctx.sender);
        } catch (CommandAccessManager.CommandsNotExistsException e) {
            ctx.replyToMessage("❌ Ошибка! Следующие команды не существуют:\n\n" + String.join("\n", e.getCommands()))
                    .callAsync(ctx.sender);
        }
    }

}
