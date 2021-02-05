package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class CakeCallback implements CallbackExecutor {

    private final CommonAbsSender telegram;


    public CakeCallback(CommonAbsSender telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return Callbacks.CAKE;
    }

    @Override
    public void execute(CallbackQuery query) {
        if (!query.getFrom().getId().equals(query.getMessage().getReplyToMessage().getFrom().getId())) {
            ApiRequests.answerCallbackQuery(query, "Этот тортик не вам!").callAsync(telegram);
            return;
        }

        if (query.getMessage().getDate() + 2400 < System.currentTimeMillis() / 1000) {
            cakeIsRotten(query);
            return;
        }

        var action = query.getData().split("\\s+")[1];
        if (action.equals("accept"))
            acceptCake(query);
        else if (action.equals("decline"))
            declineCake(query);
    }

    private void acceptCake(CallbackQuery query) {
        ApiRequests.answerCallbackQuery(query, "П p u я т н o г o  a п п e т u т a").callAsync(telegram);
        ApiRequests.editMessage(query, formatEditedMessage(query, "\uD83C\uDF82 %s принял тортик %s")).callAsync(telegram);
    }

    private void declineCake(CallbackQuery query) {
        ApiRequests.answerCallbackQuery(query, "Ну и ладно :(").callAsync(telegram);
        ApiRequests.editMessage(query,
                formatEditedMessage(query, "\uD83D\uDEAB \uD83C\uDF82 %s отказался от тортика %s")
        ).callAsync(telegram);
    }

    private void cakeIsRotten(CallbackQuery query) {
        ApiRequests.answerCallbackQuery(query, "Тортик испортился!").callAsync(telegram);
        ApiRequests.editMessage(query, "\uD83E\uDD22 Тортик попытались взять, но он испортился!").callAsync(telegram);
    }

    private String formatEditedMessage(CallbackQuery query, String format) {
        String cakeInsides = query.getMessage()
                .getText()
                .split("тортик", 2)[1]
                .strip();
        return String.format(format, query.getFrom().getFirstName(), cakeInsides);
    }
}
