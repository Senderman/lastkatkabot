package com.senderman.lastkatkabot.command.user;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.Feedback;
import com.senderman.lastkatkabot.repository.FeedbackRepository;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class SendFeedback implements CommandExecutor {

    private final ApiRequests telegram;
    private final FeedbackRepository feedbackRepo;
    private final long feedbackChannelId;

    public SendFeedback(
            ApiRequests telegram,
            FeedbackRepository feedbackRepo,
            @Value("${mainAdminId}") long feedbackChannelId
    ) {
        this.telegram = telegram;
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
    public void execute(Message message) {
        var chatId = message.getChatId();
        if (message.getText().strip().equals(getTrigger())) {
            telegram.sendMessage(chatId, "Неверное кол-во аргументов!", message.getMessageId());
            return;
        }
        var feedbackText = Html.htmlSafe(message.getText().split("\\s+", 2)[1]);
        if (feedbackText.length() > 2000) {
            telegram.sendMessage(chatId, "Максимальная длина текста - 2000 символов!", message.getMessageId());
            return;
        }
        var user = message.getFrom();
        var userLink = Html.getUserLink(user);
        int feedbackId = feedbackRepo.findFirstByOrderByIdDesc().map(f -> f.getId() + 1).orElse(1);

        var feedback = new Feedback(feedbackId, feedbackText, user.getId(), userLink, chatId, message.getMessageId());
        feedbackRepo.save(feedback);

        var text = "\uD83D\uDD14 <b>Фидбек #" + feedbackId + "</b>\n\n" +
                "От: " + userLink + "\n\n" + feedbackText + "\n\n" +
                "Для ответа, введите /fresp " + feedbackId + " &lt;ваш ответ&gt;";
        telegram.sendMessage(feedbackChannelId, text);
        telegram.sendMessage(chatId, "✅ Сообщение отправлено разработчикам!", message.getMessageId());
    }
}
