package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
public class WhereUser implements CommandExecutor {

    private final ChatUserService chatUsers;
    private final ExecutorService threadPool;

    public WhereUser(ChatUserService chatUsers, ExecutorService threadPool) {
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
    }

    @Override
    public String getDescription() {
        return "в каких чатах сидит юзер. реплай или " + command() + " userId";
    }

    @Override
    public String command() {
        return "/wru";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
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
                    .collect(Collectors.joining("\n"));

            ctx.replyToMessage("🕵️‍♂ Юзер замечен в следующих чатах:\n\n️" + chatNames).callAsync(ctx.sender);
        });
    }

    private String getChatNameOrNull(long chatId, CommonAbsSender telegram) {
        var chat = Methods.getChat(chatId).call(telegram);
        return chat != null ? chat.getTitle() : null;
    }
}
