package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class GrantAdminCommand implements CommandExecutor {

    private final UserManager<AdminUser> admins;


    public GrantAdminCommand(
            @Qualifier("adminManager") UserManager<AdminUser> admins) {
        this.admins = admins;
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public String command() {
        return "/grantadmin";
    }

    @Override
    public String getDescription() {
        return "выдача админа реплаем.";
    }

    @Override
    public void accept(MessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage()) {
            ctx.replyToMessage("Посвящать в админы нужно в группе и реплаем!").callAsync(ctx.sender);
            return;
        }
        var user = ctx.message().getReplyToMessage().getFrom();

        if (user.getIsBot()) {
            ctx.replyToMessage(
                    "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, участвовать в дуэлях, быть админом?")
                    .callAsync(ctx.sender);
            return;
        }

        if (admins.addUser(new AdminUser(user.getId(), user.getFirstName())))
            ctx.replyToMessage("Пользователь успешно посвящен в админы!").callAsync(ctx.sender);
        else
            ctx.replyToMessage("Не следует посвящать в админы дважды!").callAsync(ctx.sender);
    }
}




