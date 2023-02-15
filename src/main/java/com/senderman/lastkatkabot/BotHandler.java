package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.handler.CommandUpdateHandler;
import com.senderman.lastkatkabot.handler.NewMemberHandler;
import com.senderman.lastkatkabot.service.ChatPolicyEnsuringService;
import com.senderman.lastkatkabot.service.UserActivityTrackerService;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

@Singleton
public class BotHandler extends com.annimon.tgbotsmodule.BotHandler {

    private final BotConfig config;
    private final CommandUpdateHandler commandUpdateHandler;
    private final ChatUserService chatUsers;
    private final UserActivityTrackerService activityTrackerService;
    private final ChatPolicyEnsuringService chatPolicyEnsuringService;
    private final NewMemberHandler newMemberHandler;
    private final ExecutorService threadPool;
    private final Set<Long> telegramServiceUserIds;
    private final ObjectMapper messageToJsonMapper;

    public BotHandler(
            DefaultBotOptions botOptions,
            BotConfig config,
            CommandUpdateHandler commandUpdateHandler,
            ChatUserService chatUsers,
            UserActivityTrackerService activityTrackerService,
            ChatPolicyEnsuringService chatPolicyEnsuringService,
            NewMemberHandler newMemberHandler,
            @Named("generalNeedsPool") ExecutorService threadPool,
            ObjectMapper messageToJsonMapper
    ) {
        super(botOptions, config.token());
        this.config = config;
        this.commandUpdateHandler = commandUpdateHandler;
        this.chatUsers = chatUsers;
        this.activityTrackerService = activityTrackerService;
        this.chatPolicyEnsuringService = chatPolicyEnsuringService;
        this.newMemberHandler = newMemberHandler;
        this.threadPool = threadPool;
        this.messageToJsonMapper = messageToJsonMapper;

        this.telegramServiceUserIds = Set.of(
                777000L, // attached channel's messages
                1087968824L, // anonymous group admin @GroupAnonymousBot
                136817688L // Channel message, @Channel_Bot
        );

        addMethodPreprocessor(SendMessage.class, m -> {
            m.enableHtml(true);
            m.disableWebPagePreview();
        });

        addMethodPreprocessor(EditMessageText.class, m -> m.enableHtml(true));

        Methods.sendMessage(config.notificationChannelId(), "\n\nБот запущен!").callAsync(this);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (var update : updates) {
            try {
                onUpdate(update);
            } catch (RejectedExecutionException e) { // may occur on restart
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
                messageToJsonMapper.writeValue(pw, update);
            pw.close();
            try (var bais = new ByteArrayInputStream(baos.toByteArray())) {
                var date = ZonedDateTime.now(ZoneId.of(config.timezone())).format(DateTimeFormatter.ISO_INSTANT);
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

            threadPool.execute(() -> chatPolicyEnsuringService.queueViolationCheck(message.getChatId()));


            {
                var newMembers = message.getNewChatMembers();
                if (!newMembers.isEmpty()) {
                    threadPool.execute(() -> newMemberHandler.accept(new MessageContext(this, update, "")));
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

    private void processLeftChatMember(Message message) {
        chatUsers.deleteByChatIdAndUserId(message.getChatId(), message.getLeftChatMember().getId());
        Methods.sendDocument(message.getChatId())
                .setReplyToMessageId(message.getMessageId())
                .setFile(config.leaveStickerId())
                .callAsync(this);
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

}
