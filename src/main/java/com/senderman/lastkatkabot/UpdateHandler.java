package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.AdminUserRepository;
import com.senderman.lastkatkabot.repository.BlacklistedUserRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.service.HandlerExtractor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootApplication
public class UpdateHandler extends BotHandler {

    private final String username;
    private final String token;
    private final HandlerExtractor<CommandExecutor> commands;
    private final HandlerExtractor<CallbackExecutor> callbacks;
    private final int mainAdminId;
    private final Set<Integer> adminIds;
    private final Set<Integer> blacklist;
    private final ChatUserRepository chatUsers;

    @Autowired
    public UpdateHandler(
            @Value("${login}") String login,
            @Value("${mainAdminId}") int mainAdminId,
            @Lazy HandlerExtractor<CommandExecutor> commandExtractor,
            @Lazy HandlerExtractor<CallbackExecutor> callbacks,
            AdminUserRepository admins,
            BlacklistedUserRepository blacklist,
            ChatUserRepository chatUsers
    ) {
        var args = login.split("\\s+");
        username = args[0];
        token = args[1];

        this.commands = commandExtractor;
        this.callbacks = callbacks;
        this.mainAdminId = mainAdminId;
        this.chatUsers = chatUsers;


        this.blacklist = StreamSupport.stream(blacklist.findAll().spliterator(), false)
                .map(BlacklistedUser::getUserId)
                .collect(Collectors.toSet());

        this.adminIds = StreamSupport.stream(admins.findAll().spliterator(), false)
                .map(AdminUser::getUserId)
                .collect(Collectors.toSet());
        adminIds.add(mainAdminId);

        Methods.sendMessage(mainAdminId, "Бот запущен!").call(this);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates
                .stream()
                .filter(this::filterUpdate)
                .forEach(this::onUpdateReceived);
    }

    @Override
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {

        if (update.hasCallbackQuery()) {
            var query = update.getCallbackQuery();
            callbacks.findHandler(query.getData().split("\\s+", 2)[0])
                    .ifPresent(e -> e.execute(query));
            return null;
        }

        if (!update.hasMessage()) return null;

        var message = update.getMessage();

        // track users activity in chats
        if (!message.isUserMessage()) {
            updateUserLastMessageDate(message);
        }

        if (!message.hasText()) return null;

        var text = message.getText();

        if (!message.isCommand()) return null;

        /* bot should only trigger on general commands (like /command) or on commands for this bot (/command@mybot),
         * and NOT on commands for another bots (like /command@notmybot)
         */
        var command = text.split("\\s+", 2)[0]
                .toLowerCase(Locale.ENGLISH)
                .replace("@" + getBotUsername(), "");
        if (command.contains("@")) return null;

        commands.findHandler(command)
                .filter(e -> checkAccess(e.getRoles(), message.getFrom().getId()))
                .ifPresent(e -> e.execute(message));

        return null;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }


    private void updateUserLastMessageDate(Message message) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var date = message.getDate();

        var chatUser = chatUsers.findByChatIdAndUserId(chatId, userId).orElse(new ChatUser(userId, chatId));
        chatUser.setLastMessageDate(date);
        chatUsers.save(chatUser);
    }


    private boolean checkAccess(EnumSet<Role> roles, int userId) {
        // allow all commands for the main admin
        if (userId == mainAdminId) return true;
        // do not allow blacklisted users
        if (blacklist.contains(userId)) return false;
        // allow users to use user commands
        if (roles.contains(Role.USER)) return true;
        // check admin permissions
        return roles.contains(Role.ADMIN) && adminIds.contains(userId);
    }

    // this method contains filtering rules for all updates
    private boolean filterUpdate(Update update) {
        if (update.hasCallbackQuery()) return true;
        var message = update.getMessage();
        if (message == null) return false;

        // Skip messages older than 2 minutes
        return message.getDate() + 120 >= System.currentTimeMillis() / 1000;
    }
}
