package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.DatabaseCleanupService;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.service.UserActivityTrackerService;
import com.senderman.lastkatkabot.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
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

    private final String username;
    private final String token;
    private final BotConfig config;
    private final Commands commands;
    private final ChatUserService chatUsers;
    private final UserActivityTrackerService activityTrackerService;
    private final DatabaseCleanupService databaseCleanupService;
    private final BncTelegramHandler bnc;
    private final ImageService imageService;
    private final ExecutorService threadPool;

    @Autowired
    public BotHandler(
            BotConfig config,
            @Lazy Commands commands,
            ChatUserService chatUsers,
            UserActivityTrackerService activityTrackerService,
            DatabaseCleanupService databaseCleanupService,
            BncTelegramHandler bnc,
            ImageService imageService,
            ExecutorService threadPool
    ) {
        this.config = config;
        this.commands = commands;
        this.chatUsers = chatUsers;
        this.activityTrackerService = activityTrackerService;
        this.databaseCleanupService = databaseCleanupService;
        this.bnc = bnc;
        this.imageService = imageService;
        this.threadPool = threadPool;

        var loginArgs = config.login().split("\\s+");
        username = loginArgs[0];
        token = loginArgs[1];

        addMethodPreprocessor(SendMessage.class, m -> {
            m.enableHtml(true);
            m.disableWebPagePreview();
        });

        addMethodPreprocessor(EditMessageText.class, m -> {
            m.enableHtml(true);
            m.disableWebPagePreview();
        });

        Methods.sendMessage(config.notificationChannelId(), "Инициализация и очистка БД...").callAsync(this);
        cleanupDatabase();
        Methods.sendMessage(config.notificationChannelId(), "Бот запущен!").callAsync(this);
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
            if (!message.isUserMessage() && !message.getFrom().getId().equals(777000L)) {
                threadPool.execute(() -> updateUserLastMessageDate(message));
            }

            if (message.getLeftChatMember() != null) {
                processLeftChatMember(message);
                return null;
            }

        }

        commands.handleUpdate(update);

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
                //noinspection ResultOfMethodCallIgnored
                Methods.sendDocument(chatId)
                        .setReplyToMessageId(messageId)
                        .setFile(file)
                        .callAsync(this, m -> file.delete());
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

        activityTrackerService.updateLastMessageDate(chatId, userId, date);
    }

    private void cleanupDatabase() {
        var r = databaseCleanupService.cleanAll();
        var text = """
                ♻️ <b>Результаты очистки БД</b>

                👤 Пользователи: %d
                👥 Чаты: %d
                🐮 BnC: %d
                💒 Запросы в ЗАГС: %d"""
                .formatted(r.getUsers(), r.getChats(), r.getBncGames(), r.getMarriageRequests());
        Methods.sendMessage(config.notificationChannelId(), text).callAsync(this);
    }


}
