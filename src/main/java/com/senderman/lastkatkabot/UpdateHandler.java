package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.AdminUserRepository;
import com.senderman.lastkatkabot.repository.BlacklistedUserRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.service.HandlerExtractor;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
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
    private final BncTelegramHandler bnc;
    private final ImageService imageService;

    @Autowired
    public UpdateHandler(
            @Value("${login}") String login,
            @Value("${mainAdminId}") int mainAdminId,
            @Lazy HandlerExtractor<CommandExecutor> commandExtractor,
            @Lazy HandlerExtractor<CallbackExecutor> callbacks,
            AdminUserRepository admins,
            BlacklistedUserRepository blacklist,
            ChatUserRepository chatUsers,
            @Lazy BncTelegramHandler bnc,
            ImageService imageService
    ) {
        var args = login.split("\\s+");
        username = args[0];
        token = args[1];

        this.commands = commandExtractor;
        this.callbacks = callbacks;
        this.mainAdminId = mainAdminId;
        this.chatUsers = chatUsers;
        this.bnc = bnc;
        this.imageService = imageService;


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
        var updatesToProcess = updates.stream().filter(this::filterUpdate).iterator();
        while (updatesToProcess.hasNext()) {
            try {
                onUpdate(updatesToProcess.next());
            } catch (Throwable e) {
                Methods.sendMessage()
                        .setChatId(mainAdminId)
                        .setText("⚠️ <b>Ошибка обработки апдейта</b>\n\n" + ExceptionUtils.stackTraceAsString(e))
                        .enableHtml()
                        .disableWebPagePreview()
                        .call(this);
            }
        }
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

        {
            var newMembers = message.getNewChatMembers();
            if (newMembers != null && !newMembers.isEmpty()) {
                processNewChatMembers(message);
                return null;
            }
        }

        // track users activity in chats
        if (!message.isUserMessage()) {
            updateUserLastMessageDate(message);
        }

        if (message.getLeftChatMember() != null) {
            processLeftChatMember(message);
            return null;
        }

        if (!message.hasText()) return null;

        var text = message.getText();

        if (text.matches("\\d{4,10}")) {
            bnc.processBncAnswer(message);
            return null;
        }

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

    private void processNewChatMembers(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        for (var user : message.getNewChatMembers()) {
            try {
                var file = imageService.generateGreetingSticker(user.getFirstName());
                Methods.sendDocument(chatId)
                        .setReplyToMessageId(messageId)
                        .setFile(file)
                        .call(this);
                file.delete();
            } catch (ImageService.TooWideNicknameException | IOException e) {
                // fallback with greeting gif
                Methods.sendDocument(chatId)
                        .setReplyToMessageId(messageId)
                        .setFile(imageService.getHelloGifId())
                        .call(this);
            }
        }
    }

    private void processLeftChatMember(Message message) {
        chatUsers.deleteByChatIdAndUserId(message.getChatId(), message.getLeftChatMember().getId());
        Methods.sendDocument(message.getChatId())
                .setReplyToMessageId(message.getMessageId())
                .setFile(imageService.getLeaveStickerId())
                .call(this);
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
