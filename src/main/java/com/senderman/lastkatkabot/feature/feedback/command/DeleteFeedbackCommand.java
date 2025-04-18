package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Command
public class DeleteFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackRepo;
    private final BotConfig config;

    public DeleteFeedbackCommand(
            FeedbackService feedbackRepo,
            BotConfig config
    ) {
        this.feedbackRepo = feedbackRepo;
        this.config = config;
    }

    @Override
    public String command() {
        return "/fdel";
    }

    @Override
    public String getDescriptionKey() {
        return "feedback.fdel.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }
        var arg = ctx.argument(0);
        var reason = ctx.getString("feedback.fdel.missing");

        if (ctx.argumentsLength() > 1) {
            String[] args = ctx.argumentsAsString().split("\\s+", 2);
            reason = args[1];
        }

        if (arg.matches("\\d+")) {
            deleteSingleFeedback(ctx, Integer.parseInt(arg), reason);
        } else if (arg.matches("\\d+-\\d+")) {
            var args = arg.split("-");
            int from = Integer.parseInt(args[0]);
            int to = Integer.parseInt(args[1]);
            deleteFeedbackInRange(ctx, from, to);
        } else {
            ctx.replyToMessage(ctx.getString("feedback.common.feedbackIdIsNumber")).callAsync(ctx.sender);
        }
    }


    private void deleteSingleFeedback(L10nMessageContext ctx, int feedbackId, String reason) {
        if (!feedbackRepo.existsById(feedbackId)) {
            notifyNoFeedbacksFound(ctx);
            return;
        }

        feedbackRepo.deleteById(feedbackId);
        notifySuccess(ctx, ctx.getString("feedback.fdel.feedbackDeleted").formatted(feedbackId, reason));
    }

    private void deleteFeedbackInRange(L10nMessageContext ctx, int from, int to) {
        long result = feedbackRepo.deleteByIdBetween(from, to);
        if (result == 0) {
            notifyNoFeedbacksFound(ctx);
            return;
        }
        notifySuccess(ctx, ctx.getString("feedback.fdel.feedbacksDeleted").formatted(result, from, to));
    }

    private void notifySuccess(L10nMessageContext ctx, String text) {
        ctx.replyToMessage(text).callAsync(ctx.sender);
        if (!ctx.chatId().equals(config.notificationChannelId()))
            Methods.sendMessage()
                    .setChatId(config.notificationChannelId())
                    .setText("%s\n👤: %s"
                            .formatted(text, Html.getUserLink(ctx.user())))
                    .callAsync(ctx.sender);
    }

    private void notifyNoFeedbacksFound(L10nMessageContext ctx) {
        ctx.replyToMessage(ctx.getString("feedback.fdel.noFeedbacksFound")).callAsync(ctx.sender);
    }
}
