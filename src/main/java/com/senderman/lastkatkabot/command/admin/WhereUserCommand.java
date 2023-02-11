package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

@Command(
        command = "/wru",
        description = "в каких чатах сидит юзер. реплай или /wru userId",
        authority = {Role.ADMIN, Role.MAIN_ADMIN}
)
public class WhereUserCommand extends CommandExecutor {

    private final ChatUserService chatUsers;
    private final ExecutorService threadPool;

    public WhereUserCommand(ChatUserService chatUsers, @Named("generalNeedsPool") ExecutorService threadPool) {
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        long userId;
        try {
            userId = ctx.argumentsLength() > 0 ? Long.parseLong(ctx.argument(0)) : ctx.message().getReplyToMessage().getFrom().getId();
        } catch (NumberFormatException e) {
            ctx.replyToMessage("Id юзера - это число!").callAsync(ctx.sender);
            return;
        } catch (NullPointerException e) {
            ctx.replyToMessage("Введите Id юзера, либо используйте реплай").callAsync(ctx.sender);
            return;
        }
        threadPool.execute(() -> {
            var chatNames = chatUsers.findByUserId(userId)
                    .stream()
                    .map(chat -> getChatNameOrNull(chat.getChatId(), ctx.sender))
                    .filter(Objects::nonNull)
                    .toList();

            if (chatNames.isEmpty()) {
                ctx.replyToMessage("\uD83D\uDD75️\u200D♂ Юзера нет ни в одном чате с ботом!️").callAsync(ctx.sender);
                return;
            }

            ctx.replyToMessage("🕵️‍♂ Юзер замечен в следующих чатах:\n\n️" + String.join("\n", chatNames)).callAsync(ctx.sender);
        });
    }

    private String getChatNameOrNull(long chatId, CommonAbsSender telegram) {
        var chat = Methods.getChat(chatId).call(telegram);
        return chat != null ? chat.getTitle() : null;
    }
}
