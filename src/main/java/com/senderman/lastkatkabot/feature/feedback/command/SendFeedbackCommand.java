package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.access.service.AdminService;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackFormatterService;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.TelegramUsersHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Command
public class SendFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackRepo;
    private final FeedbackFormatterService feedbackFormatter;
    private final TelegramUsersHelper telegramUsersHelper;
    private final AdminService adminRepo;
    private final BotConfig config;

    public SendFeedbackCommand(
            FeedbackService feedbackRepo,
            FeedbackFormatterService feedbackFormatter,
            TelegramUsersHelper telegramUsersHelper,
            AdminService adminRepo, BotConfig config
    ) {
        this.feedbackRepo = feedbackRepo;
        this.feedbackFormatter = feedbackFormatter;
        this.telegramUsersHelper = telegramUsersHelper;
        this.adminRepo = adminRepo;
        this.config = config;
    }

    @Override
    public String command() {
        return "/feedback";
    }

    @Override
    public String getDescription() {
        return "feedback.feedback.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }
        var feedbackText = Html.htmlSafe(ctx.argument(0));
        if (feedbackText.length() > 2000) {
            ctx.replyToMessage(ctx.getString("feedback.feedback.textMaxLength")).callAsync(ctx.sender);
            return;
        }

        var user = ctx.user();
        var feedback = new Feedback(
                feedbackText,
                user.getId(),
                user.getFirstName(),
                ctx.chatId(),
                ctx.message().getChat().getTitle(),
                ctx.message().getMessageId(),
                Objects.requireNonNullElse(user.getLanguageCode(), config.getLocale().getDefaultLocale())
        );
        feedback = feedbackRepo.insert(feedback);

        // If possible send a reply message as a context first
        Integer replyMessageId = getCopyableReplyMessageId(ctx.message());
        Integer contextMessageId = null;
        if (replyMessageId != null) {
            contextMessageId = getMessageId(
                    Methods.forwardMessage(config.getNotificationChannelId(), ctx.chatId(), replyMessageId)
                            .call(ctx.sender)
            );
        }

        // Send feedback to developers
        var text = ctx.getString("feedback.feedback.message")
                .formatted(feedbackFormatter.format(feedback), feedback.getId(), listAdmins());
        Methods.sendMessage(config.getNotificationChannelId(), text)
                .setReplyToMessageId(contextMessageId)
                .callAsync(ctx.sender);

        // Notify reporter that the feedback is sent
        ctx.replyToMessage(ctx.getString("feedback.feedback.success")).callAsync(ctx.sender);
    }

    private String listAdmins() {
        return StreamSupport.stream(adminRepo.findAll().spliterator(), false)
                .map(a -> "<a href=\"tg://user?id=%d\">%s</a>".formatted(a.getUserId(), Html.htmlSafe(a.getName())))
                .collect(Collectors.joining(", "));
    }

    @Nullable
    private Integer getMessageId(@Nullable Message message) {
        if (message == null) return null;
        return message.getMessageId();
    }

    @Nullable
    private Integer getCopyableReplyMessageId(@NotNull Message message) {
        if (!message.isReply()) return null;

        final var reply = message.getReplyToMessage();
        // Others bot messages are not copyable
        if (telegramUsersHelper.isAnotherBot(reply.getFrom())) return null;

        return reply.getMessageId();
    }
}
