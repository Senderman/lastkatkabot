package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.repository.FeedbackRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class DeleteFeedback implements CommandExecutor {

    private final ApiRequests telegram;
    private final FeedbackRepository feedbackRepo;

    public DeleteFeedback(ApiRequests telegram, FeedbackRepository feedbackRepo) {
        this.telegram = telegram;
        this.feedbackRepo = feedbackRepo;
    }

    @Override
    public String getTrigger() {
        return "/fdel";
    }

    @Override
    public String getDescription() {
        return "удалить фидбек по id. " + getTrigger() + " 3";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var args = message.getText().split("\\s+", 2);
        if (args.length < 2) {
            telegram.sendMessage(chatId, "Неверное количество аргументов!", messageId);
            return;
        }

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            telegram.sendMessage(chatId, "id фидбека - это число!", messageId);
            return;
        }

        if (!feedbackRepo.existsById(feedbackId)) {
            telegram.sendMessage(chatId, "Фидбека с таким id не существует!", messageId);
            return;
        }

        feedbackRepo.deleteById(feedbackId);
        telegram.sendMessage(chatId, "Фидбек #" + feedbackId + " удален!");
    }
}
