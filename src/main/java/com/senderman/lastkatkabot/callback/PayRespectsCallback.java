package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class PayRespectsCallback implements CallbackExecutor {

    private final ApiRequests telegram;

    public PayRespectsCallback(ApiRequests telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return Callbacks.F;
    }

    @Override
    public void execute(CallbackQuery query) {

        if (query.getMessage().getText().contains(query.getFrom().getFirstName())) {
            telegram.answerCallbackQuery(
                    query,
                    "You've already payed respects! (or you've tried to pay respects to yourself)",
                    true);
            return;
        }

        var message = query.getMessage();
        telegram.answerCallbackQuery(query, "You've payed respects");
        telegram.execute(Methods.editMessageText()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setReplyMarkup(message.getReplyMarkup())
                .setText(message.getText() + "\n" + Html.htmlSafe(query.getFrom().getFirstName()) + " has payed respects"));

    }
}
