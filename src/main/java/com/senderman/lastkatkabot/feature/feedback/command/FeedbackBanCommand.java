package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.access.model.BlacklistedUser;
import com.senderman.lastkatkabot.feature.access.service.UserManager;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

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
        return "feedback.fban.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        ctx.setArgumentsLimit(2);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(ctx.argument(0));
        } catch (NumberFormatException e) {
            ctx.replyToMessage(ctx.getString("feedback.common.feedbackIdIsNumber")).callAsync(ctx.sender);
            return;
        }

        var feedbackOptional = feedbackService.findById(feedbackId);
        if (feedbackOptional.isEmpty()) {
            ctx.replyToMessage(ctx.getString("feedback.common.noSuchFeedback")).callAsync(ctx.sender);
            return;
        }
        var feedback = feedbackOptional.get();
        var badPersonId = feedback.getUserId();
        if (!blackUsers.addUser(new BlacklistedUser(badPersonId, feedback.getUserName()))) {
            ctx.replyToMessage(
                    ctx.getString("common.alreadyBadNeko")
                            .formatted(feedback.getUserName())
            ).callAsync(ctx.sender);
            return;
        }
        var reason = ctx.argument(1, ctx.getString("feedback.fban.noReason"));
        ctx.replyToMessage(ctx.getString("feedback.fban.notifySuccess").formatted(feedback.getUserName(), reason))
                .callAsync(ctx.sender);
        Methods.sendMessage(feedback.getChatId(),
                        ctx.getString("feedback.fban.notifyUser").formatted(reason))
                .setReplyToMessageId(feedback.getMessageId())
                .callAsync(ctx.sender);
    }
}
