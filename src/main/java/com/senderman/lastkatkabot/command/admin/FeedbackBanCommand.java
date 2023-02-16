package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.FeedbackService;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import jakarta.inject.Singleton;

import java.util.EnumSet;

@Singleton
@Command
public class FeedbackBanCommand implements CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;
    private final FeedbackService feedbackService;

    public FeedbackBanCommand(UserManager<BlacklistedUser> blackUsers, FeedbackService feedbackService) {
        this.blackUsers = blackUsers;
        this.feedbackService = feedbackService;
    }

    @Override
    public String command() {
        return "/fban";
    }

    @Override
    public String getDescription() {
        return "бан по фидбеку. /fban feedbackId причина (opt)";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(MessageContext ctx) {
        ctx.setArgumentsLimit(2);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage("Неверное кол-во аргументов!").callAsync(ctx.sender);
            return;
        }

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(ctx.argument(0));
        } catch (NumberFormatException e) {
            ctx.replyToMessage("id фидбека - это число!").callAsync(ctx.sender);
            return;
        }

        var feedbackOptional = feedbackService.findById(feedbackId);
        if (feedbackOptional.isEmpty()) {
            ctx.replyToMessage("Фидбека с таким id не существует!").callAsync(ctx.sender);
            return;
        }
        var feedback = feedbackOptional.get();
        var badPersonId = feedback.getUserId();
        if (!blackUsers.addUser(new BlacklistedUser(badPersonId, feedback.getUserName()))) {
            ctx.replyToMessage("Этот пользователь уже плохая киса!").callAsync(ctx.sender);
            return;
        }
        var reason = ctx.argument(1, "&lt;причина не указана&gt;");
        ctx.replyToMessage("Теперь %s — плохая киса! Причина: %s".formatted(feedback.getUserName(), reason))
                .callAsync(ctx.sender);
        Methods.sendMessage(feedback.getChatId(), "Разработчики добавили вас в ЧС бота. Причина: " + reason)
                .setReplyToMessageId(feedback.getMessageId())
                .callAsync(ctx.sender);
    }
}
