package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.FeedbackService;
import com.senderman.lastkatkabot.model.Feedback;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class SendFeedback implements CommandExecutor {

    private final FeedbackService feedbackRepo;
    private final long feedbackChannelId;

    public SendFeedback(
            FeedbackService feedbackRepo,
            @Value("${feedbackChannelId}") long feedbackChannelId
    ) {
        this.feedbackRepo = feedbackRepo;
        this.feedbackChannelId = feedbackChannelId;
    }

    @Override
    public String getTrigger() {
        return "/feedback";
    }

    @Override
    public String getDescription() {
        return "отправить сообщение разработчику. Например, " + getTrigger() + " бот не работает";
    }

    @Override
    public void execute(Message message, CommonAbsSender telegram) {
        var chatId = message.getChatId();
        if (message.getText().strip().equals(getTrigger())) {
            ApiRequests.answerMessage(message, "Неверное кол-во аргументов!").callAsync(telegram);
            return;
        }
        var feedbackText = Html.htmlSafe(message.getText().split("\\s+", 2)[1]);
        if (feedbackText.length() > 2000) {
            ApiRequests.answerMessage(message, "Максимальная длина текста - 2000 символов!").callAsync(telegram);
            return;
        }
        var user = message.getFrom();
        var userLink = Html.getUserLink(user);

        var feedback = feedbackRepo.insert(new Feedback(feedbackText, user.getId(), userLink, chatId, message.getMessageId()));
        var feedbackId = feedback.getId();

        var text = ("""
                🔔 <b>Фидбек #%d</b>

                От: %s

                %s

                Для ответа, введите /fresp %d &lt;ваш ответ&gt;""")
                .formatted(feedbackId, userLink, feedbackText, feedbackId);
        Methods.sendMessage(feedbackChannelId, text).callAsync(telegram);
        ApiRequests.answerMessage(message, "✅ Сообщение отправлено разработчикам!").callAsync(telegram);
    }
}
