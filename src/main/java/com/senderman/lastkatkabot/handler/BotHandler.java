package com.senderman.lastkatkabot.handler;

import com.annimon.tgbotsmodule.analytics.UpdateHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.access.service.ChatPolicyEnsuringService;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import com.senderman.lastkatkabot.feature.localization.service.LocalizationService;
import com.senderman.lastkatkabot.feature.members.service.NewMemberHandler;
import com.senderman.lastkatkabot.feature.tracking.service.ChatUserService;
import com.senderman.lastkatkabot.feature.tracking.service.UserActivityTrackerService;
import com.senderman.lastkatkabot.util.TelegramUsersHelper;
import io.micrometer.core.annotation.Counted;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@Singleton
public class BotHandler extends com.annimon.tgbotsmodule.BotHandler {

    private final static Logger logger = LoggerFactory.getLogger(BotHandler.class);

    private final BotConfig config;
    private final UpdateHandler commandRegistry;
    private final ChatUserService chatUsers;
    private final UserActivityTrackerService activityTrackerService;
    private final ChatPolicyEnsuringService chatPolicyEnsuringService;
    private final NewMemberHandler newMemberHandler;
    private final ExecutorService threadPool;
    private final TelegramUsersHelper telegramUsersHelper;
    private final ObjectMapper messageToJsonMapper;
    private final LocalizationService localizationService;

    public BotHandler(
            DefaultBotOptions botOptions,
            BotConfig config,
            UpdateHandler commandRegistry,
            ChatUserService chatUsers,
            UserActivityTrackerService activityTrackerService,
            ChatPolicyEnsuringService chatPolicyEnsuringService,
            NewMemberHandler newMemberHandler,
            TelegramUsersHelper telegramUsersHelper,
            LocalizationService localizationService,
            @Named("generalNeedsPool") ExecutorService threadPool,
            @Named("messageToJsonMapper") ObjectMapper messageToJsonMapper
    ) {
        super(botOptions, config.getToken());
        this.config = config;
        this.commandRegistry = commandRegistry;
        this.chatUsers = chatUsers;
        this.activityTrackerService = activityTrackerService;
        this.chatPolicyEnsuringService = chatPolicyEnsuringService;
        this.newMemberHandler = newMemberHandler;
        this.telegramUsersHelper = telegramUsersHelper;
        this.threadPool = threadPool;
        this.messageToJsonMapper = messageToJsonMapper;
        this.localizationService = localizationService;

        addMethodPreprocessor(SendMessage.class, m -> {
            m.enableHtml(true);
            m.disableWebPagePreview();
        });

        addMethodPreprocessor(EditMessageText.class, m -> m.enableHtml(true));

        Methods.sendMessage(config.getNotificationChannelId(), "\n\nБот запущен!").callAsync(this);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (var update : updates) {
            try {
                onUpdate(update);
            } catch (RejectedExecutionException e) { // may occur on restart
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                notifyUserAboutError(update);
                sendUpdateErrorAsFile(update, e, config.getNotificationChannelId());
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
            var message = update.getMessage();
            var chatId = message.getChatId();
            Methods.sendMessage(chatId, errorText)
                    .inReplyTo(message)
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
                messageToJsonMapper.writeValue(pw, update);
            pw.close();
            try (var bais = new ByteArrayInputStream(baos.toByteArray())) {
                var date = ZonedDateTime.now(ZoneId.of(config.getTimezone())).format(DateTimeFormatter.ISO_INSTANT);
                Methods.sendDocument()
                        .setChatId(chatId)
                        .setFile(config.getUsername() + "-" + date + ".log", bais)
                        .setCaption("⚠️ <b>Ошибка обработки апдейта</b>\n" + e.getMessage())
                        .enableHtml()
                        .call(this);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @Counted("bot_updates")
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {

        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData() == null) {
                return null;
            }
        }

        if (update.hasMessage()) {

            var message = update.getMessage();

            // do not process messages older than 2 minutes
            if (message.getDate() + 120 < System.currentTimeMillis() / 1000)
                return null;

            threadPool.execute(() -> chatPolicyEnsuringService
                    .queueViolationCheck(message.getChatId(), this::onChatViolation));

            {
                var newMembers = message.getNewChatMembers();
                if (!newMembers.isEmpty()) {
                    threadPool.execute(() -> newMemberHandler.accept(
                            new LocalizedMessageContext(this, update, "", localizationService)));
                    return null;
                }
            }

            if (message.getLeftChatMember() != null) {
                processLeftChatMember(message);
                return null;
            }

            if (telegramUsersHelper.isServiceUserId(message.getFrom())) {
                return null;
            }

            if (!message.isUserMessage()) {
                threadPool.execute(() -> updateUserLastMessageDate(message));
            }
        }

        commandRegistry.handleUpdate(this, update);

        return null;
    }

    @Override
    public void handleTelegramApiException(TelegramApiException ex) {
        // enable only if really needed
        // sendUpdateErrorAsFile(null, ex, config.notificationChannelId());
    }

    private void processLeftChatMember(Message message) {
        chatUsers.deleteByChatIdAndUserId(message.getChatId(), message.getLeftChatMember().getId());
        Methods.sendDocument(message.getChatId())
                .setReplyToMessageId(message.getMessageId())
                .setFile(config.getLeaveStickerId())
                .callAsync(this);
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }


    private void updateUserLastMessageDate(Message message) {
        var chatId = message.getChatId();
        var user = message.getFrom();
        var userId = user.getId();
        var name = user.getFirstName();
        var date = message.getDate();
        activityTrackerService.updateLastMessageDate(chatId, userId, name, date);
    }

    private void onChatViolation(long chatId) {
        Methods.sendMessage(chatId, "📛 Ваш чат в списке спамеров! Бот не хочет здесь работать!").callAsync(this);
        Methods.leaveChat(chatId).callAsync(this);
    }

}
