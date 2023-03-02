package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.access.service.AdminService;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackFormatterService;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.util.Html;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Command
public class SendFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackRepo;
    private final FeedbackFormatterService feedbackFormatter;
    private final AdminService adminRepo;
    private final BotConfig config;

    public SendFeedbackCommand(
            FeedbackService feedbackRepo,
            FeedbackFormatterService feedbackFormatter,
            AdminService adminRepo, BotConfig config
    ) {
        this.feedbackRepo = feedbackRepo;
        this.feedbackFormatter = feedbackFormatter;
        this.adminRepo = adminRepo;
        this.config = config;
    }

    @Override
    public String command() {
        return "/feedback";
    }

    @Override
    public String getDescription() {
        return "отправить сообщение разработчику. Например, /feedback бот не работает";
    }

    @Override
    public void accept(MessageContext ctx) {
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }
        var feedbackText = Html.htmlSafe(ctx.argument(0));
        if (feedbackText.length() > 2000) {
            ctx.replyToMessage("Максимальная длина текста — 2000 символов!").callAsync(ctx.sender);
            return;
        }

        var user = ctx.user();
        var feedback = new Feedback(feedbackText, user.getId(), user.getFirstName(), ctx.chatId(), ctx.message().getMessageId());
        feedback = feedbackRepo.insert(feedback);

        // If possible send a reply message as a context first
        Integer replyMessageId = getCopyableReplyMessageId(ctx.message());
        Integer contextMessageId = null;
        if (replyMessageId != null) {
            contextMessageId = getMessageId(
                    Methods.forwardMessage(config.feedbackChannelId(), ctx.chatId(), replyMessageId)
                            .call(ctx.sender)
            );
        }

        // Send feedback to developers
        var text = """
                🔔 <b>Фидбек</b> %s

                Для ответа введите <code>/fresp %d</code> &lt;ваш ответ&gt;,
                или ответом на сообщение, которое хотите отправить.
                🚨 %s""".formatted(feedbackFormatter.format(feedback), feedback.getId(), listAdmins());
        Methods.sendMessage(config.feedbackChannelId(), text)
                .setReplyToMessageId(contextMessageId)
                .callAsync(ctx.sender);

        // Notify reporter that the feedback is sent
        ctx.replyToMessage("✅ Сообщение отправлено разработчикам!").callAsync(ctx.sender);
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
        // Bot message is not copyable
        if (reply.getFrom().getIsBot()) return null;

        return reply.getMessageId();
    }
}
