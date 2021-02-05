package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.service.ChatManagerService;
import com.senderman.lastkatkabot.service.HandlerExtractor;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.service.UserManager;
import com.senderman.lastkatkabot.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

@SpringBootApplication
public class UpdateHandler extends BotHandlerExtension {

    private final String username;
    private final String token;
    private final HandlerExtractor<CommandExecutor> commands;
    private final HandlerExtractor<CallbackExecutor> callbacks;
    private final UserManager<AdminUser> admins;
    private final UserManager<BlacklistedUser> blacklist;
    private final ChatManagerService chatManagerService;
    private final int mainAdminId;
    private final ChatUserRepository chatUsers;
    private final BncTelegramHandler bnc;
    private final ImageService imageService;
    private final ExecutorService threadPool;

    @Autowired
    public UpdateHandler(
            @Value("${login}") String login,
            @Value("${mainAdminId}") int mainAdminId,
            @Lazy HandlerExtractor<CommandExecutor> commandExtractor,
            @Lazy HandlerExtractor<CallbackExecutor> callbacks,
            UserManager<AdminUser> admins,
            UserManager<BlacklistedUser> blacklist,
            ChatManagerService chatManagerService,
            ChatUserRepository chatUsers,
            @Lazy BncTelegramHandler bnc,
            ImageService imageService,
            ExecutorService threadPool
    ) {
        var args = login.split("\\s+");
        username = args[0];
        token = args[1];

        this.commands = commandExtractor;
        this.callbacks = callbacks;
        this.admins = admins;
        this.blacklist = blacklist;
        this.chatManagerService = chatManagerService;
        this.mainAdminId = mainAdminId;
        this.chatUsers = chatUsers;
        this.bnc = bnc;
        this.imageService = imageService;
        this.threadPool = threadPool;

        addMethodPreprocessor(SendMessage.PATH, m -> {
            var sm = (SendMessage) m;
            sm.enableHtml(true);
            sm.disableWebPagePreview();
        });

        Methods.sendMessage(mainAdminId, "Бот запущен!").callAsync(this);
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

        if (message.getMigrateFromChatId() != null && message.getMigrateToChatId() != null) {
            chatManagerService.migrateChatIfNeeded(message.getMigrateFromChatId(), message.getMigrateToChatId());
            return null;
        }

        {
            var newMembers = message.getNewChatMembers();
            if (!newMembers.isEmpty()) {
                processNewChatMembers(message);
                return null;
            }
        }

        // track users activity in chats
        if (!message.isUserMessage()) {
            threadPool.execute(() -> updateUserLastMessageDate(message));
        }

        if (message.getLeftChatMember() != null) {
            processLeftChatMember(message);
            return null;
        }

        if (!message.hasText()) return null;

        var text = message.getText();

        if (text.matches("(\\d|[a-fA-F]){4,16}")) {
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


    // Telegram API exceptions are often the cause of huge logs. If anything will go wrong,
    // we will catch the exception in onUpdatesReceived
    @Override
    public void handleTelegramApiException(TelegramApiException ex) {
    }


    /*
    Chat migration on SendMessage by old chat id exception. not needed for now
    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        if (!method.getMethod().equals(SendMessage.PATH))
            return super.execute(method);


        SendMessage sm = (SendMessage) method;
        try {
            return super.execute(method);
        } catch (TelegramApiRequestException e) {
            // this happens when chatId changes (when converting group to a supergroup)
            var newChatId = Optional.ofNullable(e.getParameters())
                    .map(ResponseParameters::getMigrateToChatId);
            if (newChatId.isEmpty()) {
                handleTelegramApiException(e);
                return null;
            }

            long oldChatId = Long.parseLong(sm.getChatId());
            chatManagerService.migrateChatIfNeeded(oldChatId, newChatId.get());
            sm.setChatId(Long.toString(newChatId.get()));
            return super.execute(method);
        } catch (TelegramApiException e) {
            handleTelegramApiException(e);
            return null;
        }
    }*/


    private void processNewChatMembers(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        for (var user : message.getNewChatMembers()) {
            if (user.getIsBot()) {
                continue;
            }
            try {
                var file = imageService.generateGreetingSticker(user.getFirstName());
                Methods.sendDocument(chatId)
                        .setReplyToMessageId(messageId)
                        .setFile(file)
                        .call(this);
                //noinspection ResultOfMethodCallIgnored
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

        var chatUser = chatUsers.findByChatIdAndUserId(chatId, userId).orElseGet(() -> new ChatUser(userId, chatId));
        chatUser.setLastMessageDate(date);
        chatUsers.save(chatUser);
    }


    private boolean checkAccess(EnumSet<Role> roles, int userId) {
        // allow all commands for the main admin
        if (userId == mainAdminId) return true;
        // do not allow blacklisted users
        if (blacklist.hasUser(userId)) return false;
        // allow users to use user commands
        if (roles.contains(Role.USER)) return true;
        // check admin permissions
        return roles.contains(Role.ADMIN) && admins.hasUser(userId);
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
