package com.senderman.lastkatkabot.feature.feedback.command;

import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackFormatterService;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Command
public class ShowFeedbacksCommand implements CommandExecutor {

    private static final String feedbackSeparator = "\n\n<code>====================================</code>\n\n";
    private final FeedbackService feedbackService;
    private final FeedbackFormatterService feedbackFormatter;

    public ShowFeedbacksCommand(
            FeedbackService feedbackService,
            FeedbackFormatterService feedbackFormatter
    ) {
        this.feedbackService = feedbackService;
        this.feedbackFormatter = feedbackFormatter;
    }

    @Override
    public String command() {
        return "/feedbacks";
    }

    @Override
    public String getDescription() {
        return "feedback.feedbacks.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (feedbackService.count() == 0) {
            ctx.replyToMessage(ctx.getString("feedback.feedbacks.noFeedbacks")).callAsync(ctx.sender);
            return;
        }

        ctx.replyToMessage(ctx.getString("feedback.feedbacks.loading")).callAsync(ctx.sender);

        var text = new StringBuilder(ctx.getString("feedback.feedbacks.listTitle"));
        for (Feedback feedback : feedbackService.findAll()) {
            String formattedFeedback = feedbackFormatter.format(feedback, ctx.getLocale());
            // if maximum text length reached
            if (text.length() + feedbackSeparator.length() + formattedFeedback.length() >= 4096) {
                ctx.reply(text.toString()).callAsync(ctx.sender);
                text.setLength(0);
            }
            text.append(feedbackSeparator).append(formattedFeedback);
        }
        // send remaining feedbacks
        if (!text.isEmpty()) {
            ctx.reply(text.toString())
                    .disableNotification()
                    .callAsync(ctx.sender);
        }
    }
}
