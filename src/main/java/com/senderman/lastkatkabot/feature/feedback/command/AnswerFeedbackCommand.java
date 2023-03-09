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
import com.senderman.lastkatkabot.util.TelegramUsersHelper;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.Message;

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

    @Override
    public String getDescription() {
        return "ответить на фидбек. /fresp id текст (/fresp 4 хорошо, починим)," +
                " либо в ответ на сообщение, которое хотите переслать.";
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

        try {
            final var feedback = getFeedbackByMessage(ctx);
            final @Nullable var detailMessageId = getDetailsMessageId(ctx.message());
            answerFeedback(ctx, feedback, detailMessageId);
        } catch (FeedbackValidationException e) {
            ctx.replyToMessage(e.getMessage()).callAsync(ctx.sender);
        }
    }

    private void answerFeedback(MessageContext ctx, Feedback feedback, @Nullable Integer detailMessageId) {
        markAnswered(feedback);

        // Send the message explaining that this is a developer's feedback
        var answer = Html.htmlSafe(ctx.argument(1));
        Methods.sendMessage()
                .setChatId(feedback.getChatId())
                .setText("\uD83D\uDD14 <b>Ответ разработчика</b>\n\n" + answer)
                .setReplyToMessageId(feedback.getMessageId())
                .call(ctx.sender);
        // Send the second detail message if exists
        copyMessageIfExists(ctx, feedback.getChatId(), detailMessageId);

        notifyResponseIsSent(ctx, feedback.getId());

        // notify others about answer
        if (!ctx.chatId().equals(config.getFeedbackChannelId())) {
            var replierUsername = Html.htmlSafe(ctx.user().getFirstName());
            var answerReport = "%s ответил на фидбек #%d:\n\n%s".formatted(replierUsername, feedback.getId(), answer);
            Methods.sendMessage(config.getFeedbackChannelId(), answerReport).call(ctx.sender);
            copyMessageIfExists(ctx, config.getFeedbackChannelId(), detailMessageId);
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

    @Nullable
    private Integer getDetailsMessageId(@NotNull Message message) throws FeedbackValidationException {
        if (!message.isReply()) return null;

        final var reply = message.getReplyToMessage();
        // Others bot messages are not copyable
        if (telegramUsersHelper.isAnotherBot(reply.getFrom())) {
            throw new FeedbackValidationException("Нельзя пересылать сообщения от других ботов");
        }

        return reply.getMessageId();
    }

    private void copyMessageIfExists(MessageContext ctx, long toChatId, @Nullable Integer fromMessageId) {
        if (fromMessageId != null) {
            Methods.copyMessage(toChatId, ctx.chatId(), fromMessageId)
                    .callAsync(ctx.sender);
        }
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
