package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class AnswerFeedback implements CommandExecutor {

    private final ApiRequests telegram;
    private final FeedbackRepository feedbackRepo;
    private final long feedbackChannelId;

    public AnswerFeedback(
            ApiRequests telegram,
            FeedbackRepository feedbackRepo,
            @Value("${feedbackChannelId}") long feedbackChannelId
    ) {
        this.telegram = telegram;
        this.feedbackRepo = feedbackRepo;
        this.feedbackChannelId = feedbackChannelId;
    }

    @Override
    public String getTrigger() {
        return "/fresp";
    }

    @Override
    public String getDescription() {
        return "ответить на фидбек. " + getTrigger() + " id ответ. " + getTrigger() + " 4 хорошо, починим";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        var args = message.getText().split("\\s+", 3);
        if (args.length < 3) {
            telegram.sendMessage(chatId, "Неверное количество аргументов!", message.getMessageId());
            return;
        }

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            telegram.sendMessage(chatId, "id фидбека - это число!");
            return;
        }

        var feedbackOptional = feedbackRepo.findById(feedbackId);
        if (feedbackOptional.isEmpty()) {
            telegram.sendMessage(chatId, "Фидбека с таким id не существует!");
            return;
        }
        // feedbackRepo.deleteById(feedbackId);
        var feedback = feedbackOptional.get();
        feedback.setReplied(true);
        feedbackRepo.save(feedback);

        var answer = args[2];
        telegram.sendMessage(feedback.getChatId(),
                "\uD83D\uDD14 <b>Ответ разработчика</b>\n\n" + answer,
                feedback.getMessageId());
        telegram.sendMessage(chatId, "Ответ отправлен!", message.getMessageId());

        // notify others about answer
        if (!chatId.equals(feedbackChannelId)) {
            var replierUsername = message.getFrom().getFirstName();
            var answerReport = String.format("%s ответил на фидбек #%d:\n\n%s",
                    replierUsername, feedbackId, answer);
            telegram.sendMessage(feedbackChannelId, answerReport);
        }
    }
}
