package com.senderman.lastkatkabot.feature.help.command;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.access.model.AdminUser;
import com.senderman.lastkatkabot.feature.access.service.UserManager;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class HelpCommand implements CommandExecutor {

    private final Set<CommandExecutor> executors;
    private final UserManager<AdminUser> admins;
    private final BotConfig config;

    public HelpCommand(
            Set<CommandExecutor> commands,
            UserManager<AdminUser> admins,
            BotConfig config
    ) {
        this.executors = commands
                .stream()
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
    public String getDescriptionKey() {
        return "help.description";
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
    public void accept(@NotNull L10nMessageContext ctx) {
        try {
            trySendHelpToPm(ctx.user().getId(), ctx.sender, ctx);
            if (!ctx.message().isUserMessage())
                ctx.replyToMessage(ctx.getString("help.success")).callAsync(ctx.sender);
        } catch (TelegramApiException e) {
            ctx.replyToMessage(ctx.getString("help.openPMsPlease")).callAsync(ctx.sender);
        }
    }

    private void trySendHelpToPm(long userId, CommonAbsSender telegram, L10nMessageContext ctx) throws TelegramApiException {
        var sentMessage = telegram.execute(new SendMessage(String.valueOf(userId), ctx.getString("help.wait")));
        telegram.executeAsync(EditMessageText
                .builder()
                .chatId(Long.toString(userId))
                .text(prepareHelpText(userId, ctx))
                .messageId(sentMessage.getMessageId())
                .parseMode(ParseMode.HTML)
                .build()
        );
    }

    private String prepareHelpText(long userId, L10nMessageContext ctx) {
        var locale = ctx.getLocale();
        var userHelp = new StringBuilder(ctx.getString("help.userCommands"));
        var adminHelp = new StringBuilder(ctx.getString("help.adminCommands"));
        var mainAdminHelp = new StringBuilder(ctx.getString("help.mainAdminCommands"));
        boolean userIsMainAdmin = userId == config.mainAdminId();
        boolean userIsAdmin = userIsMainAdmin || admins.hasUser(userId);

        var exeIterator = executors.stream().sorted(Comparator.comparing(CommandExecutor::command)).iterator();
        while (exeIterator.hasNext()) {
            var exe = exeIterator.next();
            var roles = exe.authority();
            if (roles.contains(Role.USER)) {
                userHelp.append(formatExecutor(exe, ctx)).append("\n");
            } else if (userIsAdmin && roles.contains(Role.ADMIN))
                adminHelp.append(formatExecutor(exe, ctx)).append("\n");
            else if (userIsMainAdmin && roles.contains(Role.MAIN_ADMIN))
                mainAdminHelp.append(formatExecutor(exe, ctx)).append("\n");

        }

        if (userIsAdmin) {
            userHelp.append("\n\n").append(adminHelp);
        }
        if (userIsMainAdmin) {
            userHelp.append("\n\n").append(mainAdminHelp);
        }

        return userHelp.toString();
    }

    private String formatExecutor(CommandExecutor executor, L10nMessageContext ctx) {
        return executor.command() + " - " + Html.htmlSafe(ctx.getString(executor.getDescriptionKey()));
    }
}
