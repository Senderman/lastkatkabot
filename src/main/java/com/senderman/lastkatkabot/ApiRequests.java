package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.api.methods.interfaces.Method;
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.service.ChatManagerService;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.ResponseParameters;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.Serializable;
import java.util.Optional;

@Component
public class ApiRequests {

    private final CommonAbsSender telegram;
    private final ChatManagerService chatManager;

    public ApiRequests(CommonAbsSender telegram, ChatManagerService chatManager) {
        this.telegram = telegram;
        this.chatManager = chatManager;
    }

    public <T extends Serializable> T execute(Method<T> method) {
        return method.call(telegram);
    }

    public <T extends Serializable> T tryExecute(BotApiMethod<T> method) throws TelegramApiException {
        return telegram.execute(method);
    }

    public Message sendMessage(long chatId, String text, @Nullable Integer replyToMessageId) {
        return sendMessage(Methods.sendMessage(chatId, text).setReplyToMessageId(replyToMessageId));
    }

    public Message sendMessage(long chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    @Nullable
    public Message sendMessage(SendMessageMethod sm) {
        // convert SendMessageMethod to SendMessage
        var sendMessage = SendMessage.builder()
                .chatId(sm.getChatId())
                .text(sm.getText())
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .replyMarkup(sm.getReplyMarkup())
                .replyToMessageId(sm.getReplyToMessageId())
                .build();

        try {
            return tryExecute(sendMessage);
        } catch (TelegramApiRequestException e) {
            // this happens when chatId changes (when converting group to a supergroup)
            var newChatId = Optional.ofNullable(e.getParameters())
                    .map(ResponseParameters::getMigrateToChatId);
            if (newChatId.isEmpty()) {
                e.printStackTrace();
                return null;
            }

            long oldChatId = Long.parseLong(sendMessage.getChatId());
            chatManager.migrateChatIfNeeded(oldChatId, newChatId.get());
            sm.setChatId(newChatId.get())
                    .enableHtml()
                    .disableWebPagePreview();
            return execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteMessage(long chatId, int messageId) {
        execute(Methods.deleteMessage(chatId, messageId));
    }

    public void answerCallbackQuery(CallbackQuery query, String text) {
        answerCallbackQuery(query, text, false);
    }

    public void answerCallbackQuery(CallbackQuery query, String text, boolean showAlert) {
        Methods.answerCallbackQuery(query.getId())
                .setText(text)
                .setShowAlert(showAlert)
                .call(telegram);
    }

    public void editQueryMessage(CallbackQuery query, String text) {
        var message = query.getMessage();
        Methods.editMessageText()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setText(text)
                .call(telegram);
    }

}
