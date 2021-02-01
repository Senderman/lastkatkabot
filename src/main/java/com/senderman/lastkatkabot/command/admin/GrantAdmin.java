package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.service.UserManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class GrantAdmin implements CommandExecutor {

    private final ApiRequests telegram;
    private final UserManager<AdminUser> admins;


    public GrantAdmin(
            ApiRequests telegram,
            @Qualifier("adminManager") UserManager<AdminUser> admins) {
        this.telegram = telegram;
        this.admins = admins;
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public String getTrigger() {
        return "/grantadmin";
    }

    @Override
    public String getDescription() {
        return "выдача админа реплаем.";
    }

    @Override
    public void execute(Message message) {
        long chatId = message.getChatId();
        var messageId = message.getMessageId();

        if (!message.isReply() || message.isUserMessage()) {
            telegram.sendMessage(chatId, "Посвящать в админы нужно в группе и реплаем!");
            return;
        }
        var user = message.getReplyToMessage().getFrom();

        if (user.getIsBot()) {
            telegram.sendMessage(chatId, "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть админом?");
            return;
        }

        if (admins.addUser(new AdminUser(user.getId())))
            telegram.sendMessage(chatId, "Пользователь успешно посвящен в админы!", messageId);
        else
            telegram.sendMessage(chatId, "Не следует посвящать в админы дважды!", messageId);

    }
}




