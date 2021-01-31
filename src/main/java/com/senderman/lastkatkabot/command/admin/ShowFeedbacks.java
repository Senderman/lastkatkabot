package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.Feedback;
import com.senderman.lastkatkabot.repository.FeedbackRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class ShowFeedbacks implements CommandExecutor {

    private static final String feedbackSeparator = "\n\n<code>========================================</code>\n\n";
    private final ApiRequests telegram;
    private final FeedbackRepository feedbackRepo;

    public ShowFeedbacks(ApiRequests telegram, FeedbackRepository feedbackRepo) {
        this.telegram = telegram;
        this.feedbackRepo = feedbackRepo;
    }

    @Override
    public String getTrigger() {
        return "/feedbacks";
    }

    @Override
    public String getDescription() {
        return "показать первые n фидбеков. Без параметра - первые 10. Напр. " + getTrigger() + " 5";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        telegram.sendMessage(chatId, "Собираем фидбеки...");
        if (feedbackRepo.count() == 0) {
            telegram.sendMessage(chatId, "Фидбеков нет!");
            return;
        }

        var text = new StringBuilder("<b>Фидбеки от даунов не умеющих юзать бота</b>");
        for (Feedback feedback : feedbackRepo.findAll()) {
            String formattedFeedback = formatFeedback(feedback);
            if (text.length() + feedbackSeparator.length() + formattedFeedback.length() < 4096) {
                text.append(feedbackSeparator).append(formattedFeedback);
            } else {// if maximum text length reached
                telegram.sendMessage(chatId, text.toString());
                text.setLength(0);
                text.append(feedbackSeparator).append(formattedFeedback);
            }
        }
        // send remaining feedbacks
        if (text.length() != 0) {
            telegram.sendMessage(chatId, text.toString());
        }
    }

    private String formatFeedback(Feedback feedback) {
        return String.format("<code>#%d</code> от %s (id<code>%d</code>)\n\n%s",
                feedback.getId(), feedback.getUserName(), feedback.getUserId(), feedback.getMessage());
    }
}
