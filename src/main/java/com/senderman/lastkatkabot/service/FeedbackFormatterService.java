package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.model.Feedback;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;

@Singleton
public class FeedbackFormatterService {

    public String format(Feedback feedback) {
        return """
                <code>#%d</code>
                От %s (id<code>%d</code>)%s
                Отвечен: %s

                %s"""
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
        if (feedback.getUserId() != feedback.getChatId()) {
            return " из чата <code>%d</code>".formatted(feedback.getChatId());
        }
        return "";
    }

    private String formatRepliedLine(Feedback feedback) {
        final int id = feedback.getId();
        if (feedback.isReplied()) {
            return "✅ (удалить — <code>/fdel %d</code>)".formatted(id);
        } else {
            return "❌ (<code>/fresp %d </code>ваш ответ)".formatted(id);
        }
    }
}
