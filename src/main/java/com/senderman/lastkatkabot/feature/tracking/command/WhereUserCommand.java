package com.senderman.lastkatkabot.feature.tracking.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import com.senderman.lastkatkabot.feature.tracking.service.ChatUserService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Command
public class WhereUserCommand implements CommandExecutor {

    private final ChatUserService chatUsers;
    private final ExecutorService threadPool;

    public WhereUserCommand(ChatUserService chatUsers, @Named("generalNeedsPool") ExecutorService threadPool) {
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
    }

    @Override
    public String command() {
        return "/wru";
    }

    @Override
    public String getDescription() {
        return "tracking.wru.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        long userId;
        try {
            userId = ctx.argumentsLength() > 0 ? Long.parseLong(ctx.argument(0)) : ctx.message().getReplyToMessage().getFrom().getId();
        } catch (NumberFormatException e) {
            ctx.replyToMessage(ctx.getString("tracking.wru.idIsNumber")).callAsync(ctx.sender);
            return;
        } catch (NullPointerException e) {
            ctx.replyToMessage(ctx.getString("tracking.wru.userIdOrReply")).callAsync(ctx.sender);
            return;
        }
        threadPool.execute(() -> {
            var chatNames = chatUsers.findByUserId(userId)
                    .stream()
                    .map(chat -> getChatNameOrChatId(chat.getChatId(), ctx.sender))
                    .collect(Collectors.joining("\n"));

            if (chatNames.isEmpty()) {
                ctx.replyToMessage(ctx.getString("tracking.wru.userNotFound")).callAsync(ctx.sender);
                return;
            }

            ctx.replyToMessage(ctx.getString("tracking.wru.userFound").formatted(chatNames)).callAsync(ctx.sender);
        });
    }

    // get chat name. If unable to get if from tg, return chatId as string
    private String getChatNameOrChatId(long chatId, CommonAbsSender telegram) {
        var chat = Methods.getChat(chatId).call(telegram);
        return chat != null ? Html.htmlSafe(chat.getTitle()) + " (<code>%d</code>)".formatted(chatId) : String.valueOf(chatId);
    }
}
