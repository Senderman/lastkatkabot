package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserManager;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.service.TriggerHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Help implements CommandExecutor {

    private final Set<CommandExecutor> executors;
    private final UserManager<AdminUser> admins;
    private final int mainAdminId;

    public Help(@Lazy Set<CommandExecutor> executors,
                UserManager<AdminUser> admins,
                @Value("${mainAdminId}") int mainAdminId
    ) {
        this.executors = executors.stream()
                .filter(CommandExecutor::showInHelp)
                .collect(Collectors.toSet());
        this.admins = admins;
        this.mainAdminId = mainAdminId;
    }

    @Override
    public String getTrigger() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "помощь";
    }

    @Override
    public boolean showInHelp() {
        return false;
    }

    @Override
    public void execute(MessageContext ctx) {
        try {
            trySendHelpToPm(ctx.user().getId(), ctx.sender);
            if (!ctx.message().isUserMessage())
                ctx.replyToMessage("✅ Помощь отправлена вам в лс!").callAsync(ctx.sender);
        } catch (TelegramApiException e) {
            ctx.replyToMessage("Пожалуйста, начните диалог со мной в лс").callAsync(ctx.sender);
        }
    }

    private void trySendHelpToPm(long userId, CommonAbsSender telegram) throws TelegramApiException {
        var sentMessage = telegram.execute(new SendMessage(String.valueOf(userId), "Подождите..."));
        telegram.executeAsync(EditMessageText
                .builder()
                .chatId(Long.toString(userId))
                .text(prepareHelpText(userId))
                .messageId(sentMessage.getMessageId())
                .build()
        );
    }

    private String prepareHelpText(long userId) {
        var userHelp = new StringBuilder("<b>Основные команды:</b>\n\n");
        var adminHelp = new StringBuilder("<b>Команды админов:</b>\n\n");
        var mainAdminHelp = new StringBuilder("<b>Команды главного админа:</b>\n\n");
        boolean userIsMainAdmin = userId == mainAdminId;
        boolean userIsAdmin = userIsMainAdmin || admins.hasUser(userId);

        var exeIterator = executors.stream().sorted(Comparator.comparing(TriggerHandler::getTrigger)).iterator();
        while (exeIterator.hasNext()) {
            var exe = exeIterator.next();
            var roles = exe.getRoles();
            if (roles.contains(Role.USER)) {
                userHelp.append(formatExecutor(exe)).append("\n");
            } else if (userIsAdmin && roles.contains(Role.ADMIN))
                adminHelp.append(formatExecutor(exe)).append("\n");
            else if (userIsMainAdmin && roles.contains(Role.MAIN_ADMIN))
                mainAdminHelp.append(formatExecutor(exe)).append("\n");

        }

        if (userIsAdmin) {
            userHelp.append("\n\n").append(adminHelp);
        }
        if (userIsMainAdmin) {
            userHelp.append("\n\n").append(mainAdminHelp);
        }

        return userHelp.toString();
    }

    private String formatExecutor(CommandExecutor executor) {
        return executor.getTrigger() + " - " + executor.getDescription();
    }
}
