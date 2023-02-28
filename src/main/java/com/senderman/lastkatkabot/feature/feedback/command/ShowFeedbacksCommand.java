package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackFormatterService;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Singleton
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
        return "показать первые n фидбеков. Без параметра - первые 10. Напр. /feedbacks 5";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        if (feedbackService.count() == 0) {
            ctx.replyToMessage("Фидбеков нет!").callAsync(ctx.sender);
            return;
        }

        ctx.replyToMessage("Собираем фидбеки...").callAsync(ctx.sender);

        var text = new StringBuilder("<b>Фидбеки от даунов не умеющих юзать бота</b>");
        for (Feedback feedback : feedbackService.findAll()) {
            String formattedFeedback = feedbackFormatter.format(feedback);
            // if maximum text length reached
            if (text.length() + feedbackSeparator.length() + formattedFeedback.length() >= 4096) {
                ctx.reply(text.toString()).callAsync(ctx.sender);
                text.setLength(0);
            }
            text.append(feedbackSeparator).append(formattedFeedback);
        }
        // send remaining feedbacks
        if (text.length() != 0) {
            ctx.reply(text.toString())
                    .disableNotification()
                    .callAsync(ctx.sender);
        }
    }
}
