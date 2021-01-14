package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.callback.Callback;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.duel.DuelController;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartDuel implements CommandExecutor {

    private final DuelController duelController;
    private final ApiRequests telegram;

    public StartDuel(DuelController duelController, ApiRequests telegram) {
        this.duelController = duelController;
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
        var sm = Methods.sendMessage()
                .setChatId(chatId)
                .setText("\uD83C\uDFAF Пользователь " + name + " начинает набор на дуэль!")
                .setReplyMarkup(new MarkupBuilder()
                        .addButton(ButtonBuilder.callbackButton()
                                .text("Присоединиться")
                                .payload(Callback.DUEL.toString()))
                        .build());

        var sentMessage = telegram.sendMessage(sm);
        if (sentMessage == null) return;
        duelController.newDuel(sentMessage.getChatId(), sentMessage.getMessageId(), user);

    }
}
