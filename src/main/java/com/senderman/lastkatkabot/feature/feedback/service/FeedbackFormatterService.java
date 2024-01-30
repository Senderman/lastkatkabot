package com.senderman.lastkatkabot.feature.feedback.service;

import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import com.senderman.lastkatkabot.util.Html;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

@Singleton
public class FeedbackFormatterService {

    private final L10nService l;
    private final BotConfig config;


    public FeedbackFormatterService(L10nService l, BotConfig config) {
        this.l = l;
        this.config = config;
    }

    /**
     * Format feedback
     *
     * @param feedback feedback to format
     * @param locale   locale for formatting. Use adminLocale if null
     * @return formatted feedback
     */
    public String format(Feedback feedback, @Nullable String locale) {

        String formatLocale = locale != null ? locale : config.getLocale().getAdminLocale();


        return l.getString("feedback.text", formatLocale)
                .formatted(
                        feedback.getId(),
                        Html.getUserLink(feedback.getUserId(), feedback.getUserName()),
                        feedback.getUserId(),
                        formatChatLine(feedback, formatLocale),
                        formatRepliedLine(feedback),
                        feedback.getUserLocale(),
                        feedback.getMessage()
                );
    }

    private String formatChatLine(Feedback feedback, String locale) {
        if (feedback.getUserId() != feedback.getChatId()) {
            return l.getString("feedback.fromChat", locale).formatted(feedback.getChatTitle(), feedback.getChatId());
        }
        return "";
    }

    private String formatRepliedLine(Feedback feedback) {
        final int id = feedback.getId();
        if (feedback.isReplied()) {
            return "✅  <code>/fdel %d</code>".formatted(id);
        } else {
            return "❌  <code>/fresp %d</code>".formatted(id);
        }
    }
}
