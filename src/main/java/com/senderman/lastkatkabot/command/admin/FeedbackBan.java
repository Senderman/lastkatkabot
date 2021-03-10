package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.FeedbackService;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class FeedbackBan implements CommandExecutor {

    private final UserManager<BlacklistedUser> blackUsers;
    private final FeedbackService feedbackService;

    public FeedbackBan(UserManager<BlacklistedUser> blackUsers, FeedbackService feedbackService) {
        this.blackUsers = blackUsers;
        this.feedbackService = feedbackService;
    }

    @Override
    public String getDescription() {
        return "бан по фидбеку. " + getTrigger() + " feedbackId причина (opt)";
    }

    @Override
    public String getTrigger() {
        return "/fban";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.MAIN_ADMIN, Role.ADMIN);
    }

    @Override
    public void execute(Message message, CommonAbsSender telegram) {
        var args = message.getText().split("\\s+", 3);
        if (args.length < 2) {
            ApiRequests.answerMessage(message, "Неверное кол-во аргументов!");
        }

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            ApiRequests.answerMessage(message, "id фидбека - это число!").callAsync(telegram);
            return;
        }

        var feedbackOptional = feedbackService.findById(feedbackId);
        if (feedbackOptional.isEmpty()) {
            ApiRequests.answerMessage(message, "Фидбека с таким id не существует!").callAsync(telegram);
            return;
        }
        var feedback = feedbackOptional.get();
        var badPersonId = feedback.getUserId();
        if (!blackUsers.addUser(new BlacklistedUser(badPersonId, feedback.getUserName()))) {
            ApiRequests.answerMessage(message, "Этот пользователь уже плохая киса!").callAsync(telegram);
            return;
        }
        var reason = args.length == 3 ? args[2] : "&lt;причина не указана&gt;";
        ApiRequests.answerMessage(message,
                "Теперь %s - плохая киса! Причина: %s".formatted(feedback.getUserName(), reason))
                .callAsync(telegram);
        Methods.sendMessage(feedback.getChatId(), "Разработчики добавили вас в ЧС бота. Причина: " + reason)
                .setReplyToMessageId(feedback.getMessageId())
                .callAsync(telegram);
    }
}
