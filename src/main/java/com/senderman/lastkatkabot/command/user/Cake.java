package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.callback.Callback;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class Cake implements CommandExecutor {

    private final ApiRequests telegram;

    public Cake(ApiRequests telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/cake";
    }

    @Override
    public String getDescription() {
        return "(reply) подарить тортик. Можно указать начинку, напр. /cake с вишней";
    }

    @Override
    public void execute(Message message) {
        if (!message.isReply() || !message.isUserMessage()) return;

        var subject = Html.htmlSafe(message.getFrom().getFirstName());
        var object = Html.htmlSafe(message.getReplyToMessage().getFrom().getFirstName());
        var text = String.format("\uD83C\uDF82 %s, пользователь %s подарил вам тортик %s",
                object, subject, message.getText().replaceAll("/@\\S*\\s?|/\\S*\\s?", ""));

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("Принять")
                        .payload(Callback.CAKE + " accept"))
                .addButton(ButtonBuilder.callbackButton()
                        .text("Отказаться")
                        .payload(Callback.CAKE + " decline"))
                .build();

        telegram.sendMessage(Methods.sendMessage(message.getChatId(), text)
                .setReplyToMessageId(message.getReplyToMessage().getMessageId())
                .setReplyMarkup(markup)
        );

    }

}
