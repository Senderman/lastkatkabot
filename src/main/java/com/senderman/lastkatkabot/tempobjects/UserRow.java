package com.senderman.lastkatkabot.tempobjects;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.TgUser;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashSet;
import java.util.Set;

public class UserRow {
    private final int divider;
    private final String name;
    private long chatId;
    private int messageId;
    private final Set<Integer> checkedUsers;
    private String messageText;

    public UserRow(Message message) throws Exception {
        var lines = message.getText().strip().split("\n");
        if (lines.length != 3)
            throw new Exception("Неверный формат");

        chatId = message.getChatId();
        String title = lines[0].split(" ",2)[1];
        name = lines[1];
        divider = Integer.parseInt(lines[2]);
        checkedUsers = new HashSet<>();
        messageText = "<b>" + title + ":</b>\n\n";
        var resultMessage = Services.handler().sendMessage(message.getChatId(), messageText);
        messageId = resultMessage.getMessageId();
        Services.db().saveRow(chatId, this);
    }

    public void addUser(Message message) {
        var user = new TgUser(message.getFrom());
        if (checkedUsers.contains(user.getId()))
            return;

        checkedUsers.add(user.getId());
        if (checkedUsers.size() % divider == 0)
            messageText += checkedUsers.size() + ". " +user.getLink() + " - " + name + "!\n";
        else
            messageText += checkedUsers.size() + ". " +user.getLink() + " - не " + name + "\n";
        updateMessage();
    }

    private void updateMessage() {
        Methods.editMessageText()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setText(messageText)
                .setParseMode(ParseMode.HTML)
                .call(Services.handler());
        Services.db().saveRow(chatId, this);
    }

    public int getMessageId() {
        return messageId;
    }
}
