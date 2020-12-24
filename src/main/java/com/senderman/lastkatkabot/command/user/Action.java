package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.UpdateHandler;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component("/action")
public class Action implements CommandExecutor {

    private final UpdateHandler telegram;

    public Action(UpdateHandler telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getDescription() {
        return "сделать действие. Действие указывать через пробел, можно реплаем";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        Methods.deleteMessage(chatId, message.getMessageId());
        if (message.getText().split("\\s+").length == 1) return;

        var action = message.getFrom().getFirstName() + " " + message.getText().split("\\s+", 2)[1];
        var sm = Methods.sendMessage(chatId, action);
        if (message.isReply()){
            sm.setReplyToMessageId(message.getReplyToMessage().getMessageId());
        }
        sm.call(telegram);
    }
}
