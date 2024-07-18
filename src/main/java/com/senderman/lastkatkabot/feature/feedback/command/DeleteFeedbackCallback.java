package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.EnumSet;
import java.util.Optional;

@Singleton
public class DeleteFeedbackCallback implements CallbackExecutor {

    public final static String NAME = "FDEL";

    private final FeedbackService feedbackRepo;
    private final BotConfig config;

    public DeleteFeedbackCallback(
            FeedbackService feedbackRepo,
            BotConfig config
    ) {
        this.feedbackRepo = feedbackRepo;
        this.config = config;
    }

    private static void editSourceMessage(L10nCallbackQueryContext ctx, String text) {
        getMessage(ctx).ifPresent(msg ->
                ctx.editMessage(msg.getText() + "\n" + text)
                        .setChatId(msg.getChatId())
                        .setMessageId(msg.getMessageId())
                        .callAsync(ctx.sender));
    }

    private static Optional<Message> getMessage(L10nCallbackQueryContext ctx) {
        final var msg = ctx.message();
        if (msg == null || msg.getChatId() == null || msg.getMessageId() == null) {
            return Optional.empty();
        }
        return Optional.of(msg);
    }

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nCallbackQueryContext ctx) {
        if (ctx.argumentsLength() < 1) return;

        var arg = ctx.argument(0);
        if (arg.equals("close")) {
            editSourceMessage(ctx, "");
            return;
        }
        if (!arg.matches("\\d+")) return;

        int feedbackId = Integer.parseInt(arg);
        if (feedbackRepo.existsById(feedbackId)) {
            feedbackRepo.deleteById(feedbackId);
            notifySuccess(ctx, ctx
                    .getString("feedback.fdel.feedbackDeleted")
                    .formatted(feedbackId, ctx.getString("feedback.fdel.missing"))
            );
        } else {
            notifyNoFeedbacksFound(ctx);
        }
    }

    private void notifySuccess(L10nCallbackQueryContext ctx, String text) {
        ctx.answerAsAlert(text).callAsync(ctx.sender);
        editSourceMessage(ctx, text);

        final var msg = getMessage(ctx);
        if (msg.isEmpty()) return;
        if (!msg.orElseThrow().getChatId().equals(config.getNotificationChannelId()))
            Methods.sendMessage()
                    .setChatId(config.getNotificationChannelId())
                    .setText(ctx.getString("feedback.fdel.notifySuccess")
                            .formatted(text, Html.getUserLink(ctx.user())))
                    .callAsync(ctx.sender);
    }

    private void notifyNoFeedbacksFound(L10nCallbackQueryContext ctx) {
        editSourceMessage(ctx, ctx.getString("feedback.fdel.noFeedbacksFound"));
    }
}
