package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.duel.DuelController;
import com.senderman.lastkatkabot.duel.SameUserException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.NoSuchElementException;

@Component
public class JoinDuel implements CallbackExecutor {

    private final DuelController duelController;
    private final CommonAbsSender telegram;

    public JoinDuel(DuelController duelController, CommonAbsSender telegram) {
        this.duelController = duelController;
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return Callbacks.DUEL;
    }

    @Override
    public void execute(CallbackQuery query) {
        var message = query.getMessage();
        try {
            duelController.joinDuel(message.getChatId(), message.getMessageId(), query.getFrom());
        } catch (SameUserException e) {
            ApiRequests.answerCallbackQuery(query,
                    "\uD83D\uDC7A Похоже, вам надо обратиться к психологу! Вы пытаетесь вызвать на дуэль самого себя!",
                    true).callAsync(telegram);
        } catch (NoSuchElementException e) {
            ApiRequests.answerCallbackQuery(query,
                    "⏰ Дуэль устарела, ищите другую!",
                    true).callAsync(telegram);
        }
    }
}
