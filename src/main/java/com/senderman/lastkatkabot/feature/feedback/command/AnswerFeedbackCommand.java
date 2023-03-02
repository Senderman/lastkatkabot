package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.feedback.exception.FeedbackValidationException;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;

import java.util.EnumSet;

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
        ctx.setArgumentsLimit(2);
        if (ctx.argumentsLength() < 2) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }
        final Feedback feedback;
        try {
            feedback = getFeedbackByMessage(ctx);
        } catch (FeedbackValidationException e) {
            ctx.replyToMessage(e.getMessage()).callAsync(ctx.sender);
            return;
        }

        markAnswered(feedback);
        if (ctx.message().isReply()) {
            answerWithTextAndMessage(ctx, feedback);
        } else {
            answerWithText(ctx, feedback);
        }
    }

    private void answerWithText(MessageContext ctx, Feedback feedback) {
        var answer = ctx.argument(1);
        Methods.sendMessage()
                .setChatId(feedback.getChatId())
                .setText("\uD83D\uDD14 <b>Ответ разработчика</b>\n\n" + answer)
                .setReplyToMessageId(feedback.getMessageId())
                .call(ctx.sender);

        notifyResponseIsSent(ctx, feedback.getId());

        // notify others about answer
        if (!ctx.chatId().equals(config.getFeedbackChannelId())) {
            var replierUsername = ctx.user().getFirstName();
            var answerReport = "%s ответил на фидбек #%d:\n\n%s".formatted(replierUsername, feedback.getId(), answer);
            Methods.sendMessage(config.getFeedbackChannelId(), answerReport).callAsync(ctx.sender);
        }
    }

    private void answerWithTextAndMessage(MessageContext ctx, Feedback feedback) {
        final var reply = ctx.message().getReplyToMessage();
        if (reply.getFrom().getIsBot()) {
            ctx.replyToMessage("Нельзя пересылать сообщения от ботов").callAsync(ctx.sender);
            return;
        }

        markAnswered(feedback);

        // Send the first message explaining that this is a developer's feedback
        Methods.sendMessage(feedback.getChatId(), "\uD83D\uDD14 <b>Ответ разработчика</b>:")
                .setReplyToMessageId(feedback.getMessageId())
                .callAsync(ctx.sender);
        // Send second detail message
        Methods.copyMessage(feedback.getChatId(), ctx.chatId(), reply.getMessageId())
                .callAsync(ctx.sender);

        notifyResponseIsSent(ctx, feedback.getId());

        // notify others about answer
        if (!ctx.chatId().equals(config.getFeedbackChannelId())) {
            var replierUsername = Html.htmlSafe(ctx.user().getFirstName());
            var answerReport = "%s ответил на фидбек #%d:".formatted(replierUsername, feedback.getId());
            Methods.sendMessage(config.getFeedbackChannelId(), answerReport).callAsync(ctx.sender);
            Methods.copyMessage(config.getFeedbackChannelId(), ctx.chatId(), reply.getMessageId())
                    .callAsync(ctx.sender);
        }
    }

    private Feedback getFeedbackByMessage(MessageContext ctx) throws FeedbackValidationException {
        int feedbackId;
        try {
            feedbackId = Integer.parseInt(ctx.argument(0));
        } catch (NumberFormatException e) {
            throw new FeedbackValidationException("id фидбека - это число!");
        }

        var feedbackOptional = feedbackService.findById(feedbackId);
        return feedbackOptional.orElseThrow(() -> new FeedbackValidationException("Фидбека с таким id не существует!"));

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
                                .payload(DeleteFeedbackCallback.NAME, feedbackId)
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text("Закрыть")
                                .payload(DeleteFeedbackCallback.NAME, "close")
                                .create()
                )
                .callAsync(ctx.sender);
    }
}
