package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.callback.Callback;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class PayRespects implements CommandExecutor {

    private final ApiRequests telegram;

    public PayRespects(ApiRequests telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/f";
    }

    @Override
    public String getDescription() {
        return "(reply) press f to pay respects. А можно вот так: /f штаны за 40 хривень. Или просто /f";
    }

    @Override
    public void execute(Message message) {

        String object;

        if (message.getText().split("\\s+").length > 1) { // when object defined in the message text
            object = "to " + message.getText().split("\\s+", 2)[1];
        } else if (message.isReply()) { // when object is another user
            object = "to " + message.getReplyToMessage().getFrom().getFirstName();
        } else {
            object = "";
        }

        telegram.deleteMessage(message.getChatId(), message.getMessageId());
        var text = "\uD83D\uDD6F Press F to pay respects " + object +
                "\n" + message.getFrom().getFirstName() + " has payed respects";

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("F")
                        .payload(Callback.F.toString()))
                .build();

        telegram.sendMessage(Methods.sendMessage(message.getChatId(), text).setReplyMarkup(markup));
    }
}
