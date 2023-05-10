package com.senderman.lastkatkabot.feature.feedback.service;

import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.localization.service.LocalizationService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;

@Singleton
public class FeedbackFormatterService {

    public LocalizationService l;

    public FeedbackFormatterService(LocalizationService l) {
        this.l = l;
    }

    public String format(Feedback feedback) {
        String locale = l.getLocale(feedback.getUserId());
        return l.getString("feedback.text", locale)
                .formatted(
                        feedback.getId(),
                        Html.getUserLink(feedback.getUserId(), feedback.getUserName()),
                        feedback.getUserId(),
                        formatChatLine(feedback),
                        formatRepliedLine(feedback),
                        feedback.getMessage()
                );
    }

    private String formatChatLine(Feedback feedback) {
        String locale = l.getLocale(feedback.getUserId());
        if (feedback.getUserId() != feedback.getChatId()) {
            return l.getString("feedback.fromChat", locale).formatted(feedback.getChatId());
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
