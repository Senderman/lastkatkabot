package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.feedback.exception.FeedbackValidationException;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.TelegramUsersHelper;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

@Command
public class AnswerFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackService;
    private final TelegramUsersHelper telegramUsersHelper;
    private final BotConfig config;

    public AnswerFeedbackCommand(
            FeedbackService feedbackService,
            TelegramUsersHelper telegramUsersHelper,
            BotConfig config
    ) {
        this.feedbackService = feedbackService;
        this.telegramUsersHelper = telegramUsersHelper;
        this.config = config;
    }

    @Override
    public String command() {
        return "/fresp";
    }

    private static void notifyResponseIsSent(L10nMessageContext ctx, int feedbackId) {
        ctx.replyToMessage(ctx.getString("feedback.fresp.success"))
                .setSingleColumnInlineKeyboard(
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("feedback.fresp.deleteFeedback"))
                                .payload(DeleteFeedbackCallback.NAME, feedbackId)
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("common.close"))
                                .payload(DeleteFeedbackCallback.NAME, "close")
                                .create()
                )
                .callAsync(ctx.sender);
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public String getDescription() {
        return "feedback.fresp.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        ctx.setArgumentsLimit(2);
        if (ctx.argumentsLength() < 2) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }

        try {
            final var feedback = getFeedbackByMessage(ctx);
            final @Nullable var detailMessageId = getDetailsMessageId(ctx);
            answerFeedback(ctx, feedback, detailMessageId);
        } catch (FeedbackValidationException e) {
            ctx.replyToMessage(e.getMessage()).callAsync(ctx.sender);
        }
    }

    private void answerFeedback(L10nMessageContext ctx, Feedback feedback, @Nullable Integer detailMessageId) {
        markAnswered(feedback);

        // Send the message explaining that this is a developer's feedback
        var answer = Html.htmlSafe(ctx.argument(1));
        Methods.sendMessage()
                .setChatId(feedback.getChatId())
                .setText(ctx.getString("feedback.fresp.developerReply").formatted(answer))
                .setReplyToMessageId(feedback.getMessageId())
                .call(ctx.sender);
        // Send the second detail message if exists
        copyMessageIfExists(ctx, feedback.getChatId(), detailMessageId);

        notifyResponseIsSent(ctx, feedback.getId());

        // notify others about answer
        if (!ctx.chatId().equals(config.getFeedbackChannelId())) {
            var replierUsername = Html.htmlSafe(ctx.user().getFirstName());
            var answerReport = ctx.getString("feedback.fresp.answerReport").formatted(replierUsername, feedback.getId(), answer);
            Methods.sendMessage(config.getFeedbackChannelId(), answerReport).call(ctx.sender);
            copyMessageIfExists(ctx, config.getFeedbackChannelId(), detailMessageId);
        }
    }

    private Feedback getFeedbackByMessage(L10nMessageContext ctx) throws FeedbackValidationException {
        int feedbackId;
        try {
            feedbackId = Integer.parseInt(ctx.argument(0));
        } catch (NumberFormatException e) {
            throw new FeedbackValidationException(ctx.getString("feedback.common.feedbackIdIsNumber"));
        }

        var feedbackOptional = feedbackService.findById(feedbackId);
        return feedbackOptional.orElseThrow(() ->
                new FeedbackValidationException(ctx.getString("feedback.common.noSuchFeedback")));

    }

    @Nullable
    private Integer getDetailsMessageId(L10nMessageContext ctx) throws FeedbackValidationException {
        if (!ctx.message().isReply()) return null;

        final var reply = ctx.message().getReplyToMessage();
        // Others bot messages are not copyable
        if (telegramUsersHelper.isAnotherBot(reply.getFrom())) {
            throw new FeedbackValidationException(ctx.getString("feedback.fresp.noBotMessages"));
        }

        return reply.getMessageId();
    }

    private void markAnswered(Feedback feedback) {
        feedback.setReplied(true);
        feedbackService.update(feedback);
    }

    private void copyMessageIfExists(L10nMessageContext ctx, long toChatId, @Nullable Integer fromMessageId) {
        if (fromMessageId != null) {
            Methods.copyMessage(toChatId, ctx.chatId(), fromMessageId)
                    .callAsync(ctx.sender);
        }
    }
}
