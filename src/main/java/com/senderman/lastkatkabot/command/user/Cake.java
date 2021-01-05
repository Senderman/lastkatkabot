package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.MethodExecutor;
import com.senderman.lastkatkabot.callback.Callback;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.TelegramHtmlUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class Cake implements CommandExecutor {

    private final MethodExecutor telegram;

    public Cake(MethodExecutor telegram) {
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

        var subject = TelegramHtmlUtils.htmlSafe(message.getFrom().getFirstName());
        var object = TelegramHtmlUtils.htmlSafe(message.getReplyToMessage().getFrom().getFirstName());
        var text = String.format("\uD83C\uDF82 %s, пользователь %s подарил вам тортик %s",
                object, subject, message.getText().replaceAll("/@\\S*\\s?|/\\S*\\s?", ""));

        telegram.sendMessage(Methods.sendMessage(message.getChatId(), text)
                .setReplyToMessageId(message.getReplyToMessage().getMessageId())
                .setReplyMarkup(createCakeMarkup())
        );

    }

    private InlineKeyboardMarkup createCakeMarkup() {
        var markup = new InlineKeyboardMarkup();

        var acceptButton = new InlineKeyboardButton("Принять");
        acceptButton.setCallbackData("CB_CAKE_OK");
        acceptButton.setCallbackData(Callback.CAKE + " accept");

        var declineButton = new InlineKeyboardButton("Отказаться");
        declineButton.setCallbackData(Callback.CAKE + " decline");

        markup.setKeyboard(List.of(List.of(acceptButton, declineButton)));
        return markup;
    }


}
