package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.DatabaseCleanupService;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.service.UserActivityTrackerService;
import com.senderman.lastkatkabot.service.fileupload.TelegramFileUploader;
import com.senderman.lastkatkabot.util.DbCleanupResults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@SpringBootApplication
@EnableScheduling
public class BotHandler extends com.annimon.tgbotsmodule.BotHandler {

    private final BotConfig config;
    private final CommandUpdateHandler commandUpdateHandler;
    private final ChatUserService chatUsers;
    private final UserActivityTrackerService activityTrackerService;
    private final DatabaseCleanupService databaseCleanupService;
    private final ImageService imageService;
    private final ExecutorService threadPool;
    private final TelegramFileUploader fileUploader;
    private final Set<Long> telegramServiceUserIds;

    @Autowired
    public BotHandler(
            DefaultBotOptions botOptions,
            BotConfig config,
            @Lazy CommandUpdateHandler commandUpdateHandler,
            ChatUserService chatUsers,
            UserActivityTrackerService activityTrackerService,
            DatabaseCleanupService databaseCleanupService,
            ImageService imageService,
            @Qualifier("generalNeedsPool") ExecutorService threadPool,
            TelegramFileUploader fileUploader
    ) {
        super(botOptions);
        this.config = config;
        this.commandUpdateHandler = commandUpdateHandler;
        this.chatUsers = chatUsers;
        this.activityTrackerService = activityTrackerService;
        this.databaseCleanupService = databaseCleanupService;
        this.imageService = imageService;
        this.threadPool = threadPool;
        this.fileUploader = fileUploader;

        this.telegramServiceUserIds = Set.of(
                777000L, // attached channel's messages
                1087968824L, // anonymous group admin @GroupAnonymousBot
                136817688L // Channel message, @Channel_Bot
        );

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
                notifyUserAboutError(update);
                sendUpdateErrorAsFile(update, e, config.notificationChannelId());
            }
        }
    }

    private void notifyUserAboutError(Update update) {
        final String errorText = "Произошла какая-то ошибка. Информация отправлена разработчикам";
        if (update.hasCallbackQuery()) {
            var query = update.getCallbackQuery();
            Methods.answerCallbackQuery(query.getId())
                    .setText(errorText)
                    .setShowAlert(true)
                    .callAsync(this);
        } else if (update.hasMessage()) {
            var messageId = update.getMessage().getMessageId();
            var chatId = update.getMessage().getChatId();
            Methods.sendMessage(chatId, errorText)
                    .setReplyToMessageId(messageId)
                    .callAsync(this);
        }
    }

    /**
     * Send error stacktrace and the update that caused it somewhere, as file
     *
     * @param update update that causes the error. Maybe be null, so it won't be shown in the sent logs
     * @param e      the error
     * @param chatId where to send
     */
    private void sendUpdateErrorAsFile(@Nullable Update update, Throwable e, long chatId) {
        try (var baos = new ByteArrayOutputStream()) {
            var pw = new PrintWriter(baos);
            e.printStackTrace(pw);
            if (update != null)
                pw.print("\n\n" + update);
            pw.close();
            try (var bais = new ByteArrayInputStream(baos.toByteArray())) {
                var date = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
                Methods.sendDocument()
                        .setChatId(chatId)
                        .setFile(config.username() + "-" + date + ".log", bais)
                        .setCaption("⚠️ <b>Ошибка обработки апдейта</b>\n" + e.getMessage())
                        .enableHtml()
                        .call(this);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {

        if (update.hasMessage()) {

            var message = update.getMessage();

            {
                var newMembers = message.getNewChatMembers();
                if (!newMembers.isEmpty()) {
                    threadPool.execute(() -> processNewChatMembers(message));
                    return null;
                }
            }

            if (message.getLeftChatMember() != null) {
                processLeftChatMember(message);
                return null;
            }
            
            if (telegramServiceUserIds.contains(message.getFrom().getId())) {
                return null;
            }

            if (!message.isUserMessage()) {
                threadPool.execute(() -> updateUserLastMessageDate(message));
            }
        }

        commandUpdateHandler.handleUpdate(update);

        return null;
    }

    @Override
    public void handleTelegramApiException(TelegramApiException ex) {
        // enable only if really needed
        // sendUpdateErrorAsFile(null, ex, config.notificationChannelId());
    }

    private void processNewChatMembers(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        for (var user : message.getNewChatMembers()) {
            if (user.getIsBot()) {
                continue;
            }
            try (var stickerStream = imageService.generateGreetingSticker(user.getFirstName())) {
                fileUploader.sendDocument(chatId, messageId, stickerStream, "sticker.webp");
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
        var user = message.getFrom();
        var userId = user.getId();
        var name = user.getFirstName();
        var date = message.getDate();


        activityTrackerService.updateLastMessageDate(chatId, userId, name, date);
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
