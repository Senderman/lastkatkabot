package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartDuel implements CommandExecutor {

    private final CommonAbsSender telegram;

    public StartDuel(CommonAbsSender telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/duel";
    }

    @Override
    public String getDescription() {
        return "Начать дуэль";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        var user = message.getFrom();
        var name = Html.htmlSafe(user.getFirstName());
        Methods.sendMessage()
                .setChatId(chatId)
                .setText("🎯 Пользователь " + name + " начинает набор на дуэль!")
                .setReplyMarkup(new MarkupBuilder()
                        .addButton(ButtonBuilder.callbackButton()
                                .text("Присоединиться")
                                .payload(Callbacks.DUEL + " " + user.getId()))
                        .build())
                .callAsync(telegram);
    }
}
