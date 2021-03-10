package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class GrantAdmin implements CommandExecutor {

    private final UserManager<AdminUser> admins;


    public GrantAdmin(
            @Qualifier("adminManager") UserManager<AdminUser> admins) {
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
    public void execute(Message message, CommonAbsSender telegram) {
        if (!message.isReply() || message.isUserMessage()) {
            ApiRequests.answerMessage(message, "Посвящать в админы нужно в группе и реплаем!").callAsync(telegram);
            return;
        }
        var user = message.getReplyToMessage().getFrom();

        if (user.getIsBot()) {
            ApiRequests.answerMessage(message, "Но это же просто бот, имитация человека! " +
                                               "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть админом?")
                    .callAsync(telegram);
            return;
        }

        if (admins.addUser(new AdminUser(user.getId(), user.getFirstName())))
            ApiRequests.answerMessage(message, "Пользователь успешно посвящен в админы!").callAsync(telegram);
        else
            ApiRequests.answerMessage(message, "Не следует посвящать в админы дважды!").callAsync(telegram);
    }
}




