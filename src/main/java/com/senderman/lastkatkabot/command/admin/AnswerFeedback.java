package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.repository.FeedbackRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class AnswerFeedback implements CommandExecutor {

    private final ApiRequests telegram;
    private final FeedbackRepository feedbackRepo;

    public AnswerFeedback(ApiRequests telegram, FeedbackRepository feedbackRepo) {
        this.telegram = telegram;
        this.feedbackRepo = feedbackRepo;
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
        feedbackRepo.deleteById(feedbackId);
        var feedback = feedbackOptional.get();
        var answer = "\uD83D\uDD14 <b>Ответ разработчика</b>\n\n" + args[2];
        telegram.sendMessage(feedback.getChatId(), answer, feedback.getMessageId());
        telegram.sendMessage(chatId, "Ответ отправлен!", message.getMessageId());
    }
}
