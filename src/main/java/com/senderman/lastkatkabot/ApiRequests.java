package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.api.methods.answerqueries.AnswerCallbackQueryMethod;
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod;
import com.annimon.tgbotsmodule.api.methods.updatingmessages.EditMessageTextMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;


public class ApiRequests {

    public static SendMessageMethod answerMessage(Message message, String text) {
        return Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText(text)
                .setReplyToMessageId(message.getMessageId());
    }

    public static AnswerCallbackQueryMethod answerCallbackQuery(CallbackQuery query, String text) {
        return answerCallbackQuery(query, text, false);
    }

    public static AnswerCallbackQueryMethod answerCallbackQuery(CallbackQuery query, String text, boolean showAlert) {
        return Methods.answerCallbackQuery()
                .setCallbackQueryId(query.getId())
                .setText(text)
                .setShowAlert(showAlert);
    }

    public static EditMessageTextMethod editMessage(Message message, String text) {
        return Methods.editMessageText()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setText(text)
                .enableHtml();
    }

    public static EditMessageTextMethod editMessage(CallbackQuery query, String text) {
        return editMessage(query.getMessage(), text);
    }

}
