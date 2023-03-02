package com.senderman.lastkatkabot.feature.help.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.access.model.AdminUser;
import com.senderman.lastkatkabot.feature.access.service.UserManager;
import com.senderman.lastkatkabot.feature.chatsettings.annotation.CommandAccessCommand;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class HelpCommand implements CommandExecutor {

    private final Set<CommandExecutor> executors;
    private final UserManager<AdminUser> admins;
    private final BotConfig config;

    public HelpCommand(
            @Command Set<CommandExecutor> commands,
            @CommandAccessCommand Set<CommandExecutor> accessCommands,
            UserManager<AdminUser> admins,
            BotConfig config
    ) {
        this.executors = Stream.concat(commands.stream(), accessCommands.stream())
                .filter(CommandExecutor::showInHelp)
                .collect(Collectors.toSet());
        this.admins = admins;
        this.config = config;
    }

    @Override
    public String command() {
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
    public Set<String> aliases() {
        return Set.of("/start");
    }

    @Override
    public void accept(MessageContext ctx) {
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
                .parseMode(ParseMode.HTML)
                .build()
        );
    }

    private String prepareHelpText(long userId) {
        var userHelp = new StringBuilder("<b>Основные команды:</b>\n\n");
        var adminHelp = new StringBuilder("<b>Команды админов:</b>\n\n");
        var mainAdminHelp = new StringBuilder("<b>Команды главного админа:</b>\n\n");
        boolean userIsMainAdmin = userId == config.getMainAdminId();
        boolean userIsAdmin = userIsMainAdmin || admins.hasUser(userId);

        var exeIterator = executors.stream().sorted(Comparator.comparing(CommandExecutor::command)).iterator();
        while (exeIterator.hasNext()) {
            var exe = exeIterator.next();
            var roles = exe.authority();
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
        return executor.command() + " - " + Html.htmlSafe(executor.getDescription());
    }
}
