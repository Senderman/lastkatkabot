package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.MethodExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class Action implements CommandExecutor {

    private final MethodExecutor telegram;

    public Action(MethodExecutor telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/action";
    }

    @Override
    public String getDescription() {
        return "сделать действие. Действие указывать через пробел, можно реплаем";
    }

    @Override
    public void execute(Message trigger) {
        var chatId = trigger.getChatId();
        Methods.deleteMessage(chatId, trigger.getMessageId());
        if (trigger.getText().split("\\s+").length == 1) return;

        var action = trigger.getFrom().getFirstName() + " " + trigger.getText().split("\\s+", 2)[1];
        var sm = Methods.sendMessage(chatId, action);
        if (trigger.isReply()) {
            sm.setReplyToMessageId(trigger.getReplyToMessage().getMessageId());
        }
        telegram.sendMessage(sm);
    }
}
