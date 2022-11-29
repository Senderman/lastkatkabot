package com.senderman.lastkatkabot.command.user.settings;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.service.CommandAccessManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.Set;

@Command(
        command = "/callow",
        description = "разрешить команды. Использование: /callow /command1 command2"
)
public class AllowCommand extends CommandExecutor {

    private final CommandAccessManager commandAccessManager;

    public AllowCommand(@Lazy CommandAccessManager commandAccessManager) {
        this.commandAccessManager = commandAccessManager;
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