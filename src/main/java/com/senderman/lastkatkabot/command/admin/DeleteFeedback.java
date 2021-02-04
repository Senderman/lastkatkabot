package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.repository.FeedbackRepository;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class DeleteFeedback implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final FeedbackRepository feedbackRepo;
    private final long feedbackChannelId;


    public DeleteFeedback(
            CommonAbsSender telegram,
            FeedbackRepository feedbackRepo,
            @Value("${feedbackChannelId}") long feedbackChannelId
    ) {
        this.telegram = telegram;
        this.feedbackRepo = feedbackRepo;
        this.feedbackChannelId = feedbackChannelId;
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
        var args = message.getText().split("\\s+", 2);
        if (args.length < 2) {
            ApiRequests.answerMessage(message, "Неверное количество аргументов!").call(telegram);
            return;
        }

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            ApiRequests.answerMessage(message, "id фидбека - это число!").call(telegram);
            return;
        }

        if (!feedbackRepo.existsById(feedbackId)) {
            ApiRequests.answerMessage(message, "Фидбека с таким id не существует!").call(telegram);
            return;
        }

        feedbackRepo.deleteById(feedbackId);
        var chatId = message.getChatId();
        var text = "Фидбек #" + feedbackId + " удален";
        Methods.sendMessage(chatId, text).call(telegram);
        if (!chatId.equals(feedbackChannelId))
            Methods.sendMessage()
                    .setChatId(feedbackChannelId)
                    .setText(text + " пользователем " + Html.getUserLink(message.getFrom()))
                    .call(telegram);
    }
}
