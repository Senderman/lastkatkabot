package com.senderman.lastkatkabot.callback;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.duel.DuelController;
import com.senderman.lastkatkabot.duel.SameUserException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.NoSuchElementException;

@Component
public class JoinDuel implements CallbackExecutor {

    private final DuelController duelController;
    private final ApiRequests telegram;

    public JoinDuel(DuelController duelController, ApiRequests telegram) {
        this.duelController = duelController;
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return Callback.DUEL.toString();
    }

    @Override
    public void execute(CallbackQuery query) {
        var message = query.getMessage();
        try {
            duelController.joinDuel(message.getChatId(), message.getMessageId(), query.getFrom());
        } catch (SameUserException e) {
            telegram.answerCallbackQuery(query,
                    "\uD83D\uDC7A Похоже, вам надо обратиться к психологу! Вы пытаетесь вызвать на дуэль самого себя!",
                    true);
        } catch (NoSuchElementException e) {
            telegram.answerCallbackQuery(query,
                    "⏰ Дуэль устарела, ищите другую!",
                    true);
        }
    }
}
