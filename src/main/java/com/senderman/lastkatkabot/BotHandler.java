package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.DatabaseCleanupService;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.service.UserActivityTrackerService;
import com.senderman.lastkatkabot.util.DbCleanupResults;
import com.senderman.lastkatkabot.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@SpringBootApplication
public class BotHandler extends com.annimon.tgbotsmodule.BotHandler {

    private final BotConfig config;
    private final CommandUpdateHandler commandUpdateHandler;
    private final ChatUserService chatUsers;
    private final UserActivityTrackerService activityTrackerService;
    private final DatabaseCleanupService databaseCleanupService;
    private final ImageService imageService;
    private final ExecutorService threadPool;

    @Autowired
    public BotHandler(
            DefaultBotOptions botOptions,
            BotConfig config,
            @Lazy CommandUpdateHandler commandUpdateHandler,
            ChatUserService chatUsers,
            UserActivityTrackerService activityTrackerService,
            DatabaseCleanupService databaseCleanupService,
            ImageService imageService,
            ExecutorService threadPool
    ) {
        super(botOptions);
        this.config = config;
        this.commandUpdateHandler = commandUpdateHandler;
        this.chatUsers = chatUsers;
        this.activityTrackerService = activityTrackerService;
        this.databaseCleanupService = databaseCleanupService;
        this.imageService = imageService;
        this.threadPool = threadPool;

        addMethodPreprocessor(SendMessage.class, m -> {
            m.enableHtml(true);
            m.disableWebPagePreview();
        });

        addMethodPreprocessor(EditMessageText.class, m -> {
            m.enableHtml(true);
            m.disableWebPagePreview();
        });

        var launchText = parseCleanupResults(cleanupDatabase()) + "\n\nБот запущен!";
        Methods.sendMessage(config.notificationChannelId(), launchText).callAsync(this);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (var update : updates) {
            try {
                onUpdate(update);
            } catch (RejectedExecutionException ignored) {
            } catch (Throwable e) {
                Methods.sendMessage()
                        .setChatId(config.notificationChannelId())
                        .setText("⚠️ <b>Ошибка обработки апдейта</b>\n\n" + ExceptionUtils.stackTraceAsString(e))
                        .enableHtml()
                        .disableWebPagePreview()
                        .callAsync(this);
            }
        }
    }

    @Override
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {

        if (update.hasMessage()) {

            var message = update.getMessage();

            if (message.getDate() + 120 < System.currentTimeMillis() / 1000) return null;

            {
                var newMembers = message.getNewChatMembers();
                if (!newMembers.isEmpty()) {
                    threadPool.execute(() -> processNewChatMembers(message));
                    return null;
                }
            }

            // track users activity in chats (777000 is userId of attached channel messages)
            // And 1087968824 is anonymous group admin
            if (
                    !message.isUserMessage()
                    && !message.getFrom().getId().equals(777000L)
                    && !message.getFrom().getId().equals(1087968824L)
            ) {
                threadPool.execute(() -> updateUserLastMessageDate(message));
            }

            if (message.getLeftChatMember() != null) {
                processLeftChatMember(message);
                return null;
            }

        }

        commandUpdateHandler.handleUpdate(update);

        return null;
    }


    // Telegram API exceptions are often the cause of huge logs. If anything goes wrong,
    // we will catch the exception in onUpdatesReceived
    @Override
    public void handleTelegramApiException(TelegramApiException ex) {
    }

    private void processNewChatMembers(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        for (var user : message.getNewChatMembers()) {
            if (user.getIsBot()) {
                continue;
            }
            try {
                var file = imageService.generateGreetingSticker(user.getFirstName());
                //noinspection ResultOfMethodCallIgnored
                Methods.sendDocument(chatId)
                        .setReplyToMessageId(messageId)
                        .setFile(file)
                        // file should be deleted on both success/failure
                        .callAsync(this, m -> file.delete(), e -> file.delete());
            } catch (ImageService.TooWideNicknameException | IOException e) {
                // fallback with greeting gif
                Methods.sendDocument(chatId)
                        .setReplyToMessageId(messageId)
                        .setFile(imageService.getHelloGifId())
                        .callAsync(this);
            }
        }
    }

    private void processLeftChatMember(Message message) {
        chatUsers.deleteByChatIdAndUserId(message.getChatId(), message.getLeftChatMember().getId());
        Methods.sendDocument(message.getChatId())
                .setReplyToMessageId(message.getMessageId())
                .setFile(imageService.getLeaveStickerId())
                .callAsync(this);
    }

    @Override
    public String getBotToken() {
        return config.token();
    }

    @Override
    public String getBotUsername() {
        return config.username();
    }


    private void updateUserLastMessageDate(Message message) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var date = message.getDate();

        activityTrackerService.updateLastMessageDate(chatId, userId, date);
    }

    private DbCleanupResults cleanupDatabase() {
        return databaseCleanupService.cleanAll();
    }

    private String parseCleanupResults(DbCleanupResults r) {
        return """
                ♻️ <b>Результаты очистки БД</b>

                👤 Пользователи: %d
                👥 Чаты: %d
                🐮 BnC: %d
                💒 Запросы в ЗАГС: %d"""
                .formatted(r.getUsers(), r.getChats(), r.getBncGames(), r.getMarriageRequests());
    }


}
