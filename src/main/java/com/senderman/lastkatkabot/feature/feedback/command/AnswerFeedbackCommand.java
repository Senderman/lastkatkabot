package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Callbacks;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;

@Singleton
@Command
public class AnswerFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackService;
    private final BotConfig config;

    public AnswerFeedbackCommand(
            FeedbackService feedbackService,
            BotConfig config
    ) {
        this.feedbackService = feedbackService;
        this.config = config;
    }

    @Override
    public String command() {
        return "/fresp";
    }

    @Override
    public String getDescription() {
        return "ответить на фидбек. /fresp id текст (/fresp 4 хорошо, починим)" +
                " или /fresp id в ответ на сообщение, которое хотите переслать. ";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(MessageContext ctx) {
        if (ctx.message().isReply()) {
            forwardFeedback(ctx);
        } else {
            textFeedback(ctx);
        }
    }

    private void textFeedback(MessageContext ctx) {
        ctx.setArgumentsLimit(2);
        if (ctx.argumentsLength() < 2) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }

        final var feedbackResult = getFeedbackOrValidationResult(ctx);
        final var feedback = feedbackResult.getLeft();
        if (feedback == null) {
            ctx.replyToMessage(feedbackResult.getRight()).callAsync(ctx.sender);
            return;
        }

        markAnswered(feedback);

        var answer = ctx.argument(1);
        Methods.sendMessage()
                .setChatId(feedback.getChatId())
                .setText("\uD83D\uDD14 <b>Ответ разработчика</b>\n\n" + answer)
                .setReplyToMessageId(feedback.getMessageId())
                .call(ctx.sender);

        notifyResponseIsSent(ctx, feedback.getId());

        // notify others about answer
        if (!ctx.chatId().equals(config.feedbackChannelId())) {
            var replierUsername = ctx.user().getFirstName();
            var answerReport = "%s ответил на фидбек #%d:\n\n%s".formatted(replierUsername, feedback.getId(), answer);
            Methods.sendMessage(config.feedbackChannelId(), answerReport).callAsync(ctx.sender);
        }
    }

    private void forwardFeedback(MessageContext ctx) {
        final var reply = ctx.message().getReplyToMessage();
        if (reply.getFrom().getIsBot()) {
            ctx.replyToMessage("Нельзя пересылать сообщения от ботов").callAsync(ctx.sender);
            return;
        }

        final var feedbackResult = getFeedbackOrValidationResult(ctx);
        final var feedback = feedbackResult.getLeft();
        if (feedback == null) {
            ctx.replyToMessage(feedbackResult.getRight()).callAsync(ctx.sender);
            return;
        }

        markAnswered(feedback);

        // Send the first message explaining that this is a developer's feedback
        Methods.sendMessage(feedback.getChatId(), "\uD83D\uDD14 <b>Ответ разработчика</b>:")
                .setReplyToMessageId(feedback.getMessageId())
                .call(ctx.sender);
        // Send second detail message
        Methods.copyMessage(feedback.getChatId(), ctx.chatId(), reply.getMessageId())
                .call(ctx.sender);

        notifyResponseIsSent(ctx, feedback.getId());

        // notify others about answer
        if (!ctx.chatId().equals(config.feedbackChannelId())) {
            var replierUsername = ctx.user().getFirstName();
            var answerReport = "%s ответил на фидбек #%d:".formatted(replierUsername, feedback.getId());
            Methods.sendMessage(config.feedbackChannelId(), answerReport).call(ctx.sender);
            Methods.copyMessage(config.feedbackChannelId(), ctx.chatId(), reply.getMessageId())
                    .call(ctx.sender);
        }
    }

    private Pair<Feedback, String> getFeedbackOrValidationResult(MessageContext ctx) {
        int feedbackId;
        try {
            feedbackId = Integer.parseInt(ctx.argument(0));
        } catch (NumberFormatException e) {
            return Pair.of(null, "id фидбека - это число!");
        }

        var feedbackOptional = feedbackService.findById(feedbackId);
        if (feedbackOptional.isEmpty()) {
            return Pair.of(null, "Фидбека с таким id не существует!");
        }

        return Pair.of(feedbackOptional.orElseThrow(), null);
    }

    private void markAnswered(Feedback feedback) {
        feedback.setReplied(true);
        feedbackService.update(feedback);
    }

    private static void notifyResponseIsSent(MessageContext ctx, int feedbackId) {
        ctx.replyToMessage("✅ Ответ отправлен!")
                .setSingleColumnInlineKeyboard(
                        ButtonBuilder.callbackButton()
                                .text("Удалить фидбек")
                                .payload(Callbacks.FEEDBACK_DELETE, feedbackId)
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text("Закрыть")
                                .payload(Callbacks.FEEDBACK_DELETE, "close")
                                .create()
                )
                .callAsync(ctx.sender);
    }
}
