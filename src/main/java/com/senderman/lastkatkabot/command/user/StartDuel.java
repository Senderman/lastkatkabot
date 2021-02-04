package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.callback.Callbacks;
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
    private final CommonAbsSender telegram;

    public StartDuel(DuelController duelController, CommonAbsSender telegram) {
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
                                .payload(Callbacks.DUEL))
                        .build());

        var sentMessage = sm.call(telegram);
        if (sentMessage == null) return;
        duelController.newDuel(sentMessage.getChatId(), sentMessage.getMessageId(), user);

    }
}
