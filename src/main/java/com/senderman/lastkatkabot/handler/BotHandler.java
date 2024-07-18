package com.senderman.lastkatkabot.handler;

import com.annimon.tgbotsmodule.BotModuleOptions;
import com.annimon.tgbotsmodule.analytics.UpdateHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.access.service.ChatPolicyEnsuringService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import com.senderman.lastkatkabot.feature.media.Media;
import com.senderman.lastkatkabot.feature.media.MediaIdService;
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
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
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
    private final L10nService l10n;
    private final UpdateHandler commandRegistry;
    private final ChatUserService chatUsers;
    private final UserActivityTrackerService activityTrackerService;
    private final ChatPolicyEnsuringService chatPolicyEnsuringService;
    private final NewMemberHandler newMemberHandler;
    private final ExecutorService threadPool;
    private final TelegramUsersHelper telegramUsersHelper;
    private final ObjectMapper messageToJsonMapper;
    private final L10nService localizationService;
    private final MediaIdService mediaIdService;

    public BotHandler(
            BotModuleOptions botOptions,
            BotConfig config,
            L10nService l10n,
            UpdateHandler commandRegistry,
            ChatUserService chatUsers,
            UserActivityTrackerService activityTrackerService,
            ChatPolicyEnsuringService chatPolicyEnsuringService,
            NewMemberHandler newMemberHandler,
            TelegramUsersHelper telegramUsersHelper,
            L10nService localizationService,
            @Named("generalNeedsPool") ExecutorService threadPool,
            @Named("messageToJsonMapper") ObjectMapper messageToJsonMapper,
            MediaIdService mediaIdService
    ) {
        super(botOptions);
        this.config = config;
        this.l10n = l10n;
        this.commandRegistry = commandRegistry;
        this.chatUsers = chatUsers;
        this.activityTrackerService = activityTrackerService;
        this.chatPolicyEnsuringService = chatPolicyEnsuringService;
        this.newMemberHandler = newMemberHandler;
        this.telegramUsersHelper = telegramUsersHelper;
        this.threadPool = threadPool;
        this.messageToJsonMapper = messageToJsonMapper;
        this.localizationService = localizationService;
        this.mediaIdService = mediaIdService;

        addMethodPreprocessor(SendMessage.class, m -> {
            m.enableHtml(true);
            m.disableWebPagePreview();
        });

        addMethodPreprocessor(EditMessageText.class, m -> m.enableHtml(true));

        Methods.sendMessage(config.getNotificationChannelId(), l10n.getAdminString("common.botStarted")).callAsync(this);
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
                sendUpdateErrorAsFile(update, e);
            }
        }
    }

    private void notifyUserAboutError(Update update) {
        if (update.hasCallbackQuery()) {
            var query = update.getCallbackQuery();
            Methods.answerCallbackQuery(query.getId())
                    .setText(l10n.getString("common.updateHandlingErrorUser", l10n.getLocale(query.getFrom().getId())))
                    .setShowAlert(true)
                    .callAsync(this);
        } else if (update.hasMessage()) {
            var message = update.getMessage();
            var chatId = message.getChatId();
            Methods.sendMessage(
                            chatId,
                            l10n.getString("common.updateHandlingErrorUser", l10n.getLocale(message.getFrom().getId()))
                    )
                    .inReplyTo(message)
                    .callAsync(this);
        }
    }

    /**
     * Send error stacktrace and the update that caused it somewhere, as file
     *
     * @param update update that causes the error. Could be null, so it won't be shown in the sent logs
     * @param e      the error
     */
    public void sendUpdateErrorAsFile(@Nullable Update update, Throwable e) {
        try (var baos = new ByteArrayOutputStream()) {
            var pw = new PrintWriter(baos);
            e.printStackTrace(pw);
            if (update != null)
                messageToJsonMapper.writeValue(pw, update);
            pw.close();
            try (var bais = new ByteArrayInputStream(baos.toByteArray())) {
                var date = ZonedDateTime.now(ZoneId.of(config.getTimezone())).format(DateTimeFormatter.ISO_INSTANT);
                Methods.sendDocument()
                        .setChatId(config.getNotificationChannelId())
                        .setFile(config.getUsername() + "-" + date + ".log", bais)
                        .setCaption(l10n.getAdminString("common.updateHandlingError") + e.getMessage())
                        .enableHtml()
                        .call(this);
            }
        } catch (IOException ex) {
            logger.error("Exception during saving exception info to file", ex);
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
                            new L10nMessageContext(this, update, "", localizationService)));
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

            threadPool.execute(() -> updateActualUserData(message));
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
        var method = Methods.Stickers.sendSticker(message.getChatId()).setReplyToMessageId(message.getMessageId());
        mediaIdService.setMedia(method, Media.LEAVE_STICKER);
        method
                .callAsync(this)
                .thenAccept(m -> mediaIdService.setFileId(Media.LEAVE_STICKER, m.getSticker().getFileId()));
    }


    private void updateActualUserData(Message message) {
        var chatId = message.getChatId();
        var user = message.getFrom();
        var userId = user.getId();
        var name = user.getFirstName();
        var date = message.getDate();
        var locale = user.getLanguageCode();
        activityTrackerService.updateActualUserData(chatId, userId, name, locale, date);
    }

    private void onChatViolation(long chatId) {
        Methods.sendMessage(chatId, l10n.getDefaultString("common.yourChatIsBad"));
        Methods.leaveChat(chatId).callAsync(this);
    }

}
