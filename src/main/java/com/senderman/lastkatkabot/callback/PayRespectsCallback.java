package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class PayRespectsCallback implements CallbackExecutor {

    public PayRespectsCallback() {
    }

    @Override
    public String getTrigger() {
        return Callbacks.F;
    }

    @Override
    public void execute(CallbackQuery query, CommonAbsSender telegram) {

        if (query.getMessage().getText().contains(query.getFrom().getFirstName())) {
            ApiRequests.answerCallbackQuery(
                    query,
                    "You've already payed respects! (or you've tried to pay respects to yourself)",
                    true)
                    .callAsync(telegram);
            return;
        }

        var message = query.getMessage();
        ApiRequests.answerCallbackQuery(query, "You've paid respects").callAsync(telegram);
        Methods.editMessageText()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setReplyMarkup(message.getReplyMarkup())
                .setText(message.getText() + "\n" + Html.htmlSafe(query.getFrom().getFirstName()) + " has paid respects")
                .callAsync(telegram);

    }
}
