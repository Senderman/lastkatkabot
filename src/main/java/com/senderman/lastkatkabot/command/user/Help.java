package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Help implements CommandExecutor {

    private final Set<CommandExecutor> executors;
    private final AdminUserRepository admins;
    private final int mainAdminId;
    private final CommonAbsSender telegram;

    public Help(@Lazy Set<CommandExecutor> executors,
                AdminUserRepository admins,
                @Value("${mainAdminId}") int mainAdminId,
                CommonAbsSender telegram
    ) {
        this.executors = executors.stream()
                .filter(CommandExecutor::showInHelp)
                .collect(Collectors.toSet());
        this.admins = admins;
        this.mainAdminId = mainAdminId;
        this.telegram = telegram;
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
    public void execute(Message message) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();

        if (!message.isUserMessage()) {
            sendHelpToPm(chatId, userId, message.getMessageId());
            return;
        }

        Methods.sendMessage(chatId, prepareHelpText(userId)).callAsync(telegram);

    }

    private void sendHelpToPm(long chatId, int userId, int chatMessageId) {
        try {
            var sentMessage = telegram.execute(new SendMessage(String.valueOf(userId), "Подождите..."));
            telegram.execute(EditMessageText
                    .builder()
                    .chatId(Integer.toString(userId))
                    .text(prepareHelpText(userId))
                    .messageId(sentMessage.getMessageId())
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .build()
            );
            Methods.sendMessage(chatId, "✅ Помощь отправлена вам в лс!")
                    .setReplyToMessageId(chatMessageId)
                    .callAsync(telegram);
        } catch (TelegramApiException e) {
            Methods.sendMessage(chatId, "Пожалуйста, начните диалог со мной в лс")
                    .setReplyToMessageId(chatMessageId)
                    .callAsync(telegram);
        }
    }

    private String prepareHelpText(int userId) {
        var userHelp = new StringBuilder("<b>Основные команды:</b>\n\n");
        var adminHelp = new StringBuilder("<b>Команды админов:</b>\n\n");
        var mainAdminHelp = new StringBuilder("<b>Команды главного админа:</b>\n\n");

        for (var exe : executors) {
            var roles = exe.getRoles();
            if (roles.contains(Role.MAIN_ADMIN))
                mainAdminHelp.append(formatExecutor(exe)).append("\n");
            else if (roles.contains(Role.ADMIN))
                adminHelp.append(formatExecutor(exe)).append("\n");
            else if (roles.contains(Role.USER))
                userHelp.append(formatExecutor(exe)).append("\n");
        }

        if (admins.existsById(userId) || userId == mainAdminId) {
            userHelp.append("\n\n").append(adminHelp);
        }
        if (userId == mainAdminId) {
            userHelp.append("\n\n").append(mainAdminHelp);
        }

        return userHelp.toString();
    }

    private String formatExecutor(CommandExecutor executor) {
        return executor.getTrigger() + " - " + executor.getDescription();
    }
}
