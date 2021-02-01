package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.repository.BlacklistedUserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class BadNeko implements CommandExecutor {

    private final ApiRequests telegram;
    private final BlacklistedUserRepository blackUsers;


    public BadNeko(ApiRequests telegram, BlacklistedUserRepository blackUsers) {
        this.telegram = telegram;
        this.blackUsers = blackUsers;
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public String getTrigger() {
        return "/badneko";
    }

    @Override
    public String getDescription() {
        return "опущение до плохой кисы. реплаем.";
    }

    @Override
    public void execute(Message message) {
        long chatId = message.getChatId();
        if (!message.isReply() || message.isUserMessage()) {
            telegram.sendMessage(chatId, "Опускать в плохие кисы нужно в группе и реплаем!");
            return;
        }
        var user = message.getReplyToMessage().getFrom();

        if (user.getIsBot()) {
            telegram.sendMessage(chatId, "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть плохой кисой?");
            return;
        }
        if (blackUsers.existsById(user.getId())) {
            telegram.sendMessage(chatId, "Он уже плохая киса!");
            return;
        }
        var blackUser = new BlacklistedUser(user.getId());
        blackUsers.save(blackUser);

        var messageId = message.getMessageId();
        telegram.sendMessage(chatId, "Теперь он плохая киса!", messageId);
    }
}




