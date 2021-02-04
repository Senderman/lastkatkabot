package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class PayRespectsCallback implements CallbackExecutor {

    private final CommonAbsSender telegram;

    public PayRespectsCallback(CommonAbsSender telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return Callbacks.F;
    }

    @Override
    public void execute(CallbackQuery query) {

        if (query.getMessage().getText().contains(query.getFrom().getFirstName())) {
            ApiRequests.answerCallbackQuery(
                    query,
                    "You've already payed respects! (or you've tried to pay respects to yourself)",
                    true)
                    .call(telegram);
            return;
        }

        var message = query.getMessage();
        ApiRequests.answerCallbackQuery(query, "You've paid respects").call(telegram);
        Methods.editMessageText()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setReplyMarkup(message.getReplyMarkup())
                .setText(message.getText() + "\n" + Html.htmlSafe(query.getFrom().getFirstName()) + " has paid respects")
                .call(telegram);

    }
}
