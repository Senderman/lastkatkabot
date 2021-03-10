package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class CakeCallback implements CallbackExecutor {


    public CakeCallback() {
    }

    @Override
    public String getTrigger() {
        return Callbacks.CAKE;
    }

    @Override
    public void execute(CallbackQuery query, CommonAbsSender telegram) {
        var args = query.getData().split("\\s+");
        if (!query.getFrom().getId().equals(Integer.parseInt(args[2]))) {
            ApiRequests.answerCallbackQuery(query, "Этот тортик не вам!", true).callAsync(telegram);
            return;
        }

        if (query.getMessage().getDate() + 2400 < System.currentTimeMillis() / 1000) {
            cakeIsRotten(query, telegram);
            return;
        }

        var action = args[1];
        if (action.equals("accept"))
            acceptCake(query, telegram);
        else if (action.equals("decline"))
            declineCake(query, telegram);
    }

    private void acceptCake(CallbackQuery query, CommonAbsSender telegram) {
        ApiRequests.answerCallbackQuery(query, "П p u я т н o г o  a п п e т u т a").callAsync(telegram);
        ApiRequests.editMessage(query, formatEditedMessage(query, "\uD83C\uDF82 %s принял тортик %s")).callAsync(telegram);
    }

    private void declineCake(CallbackQuery query, CommonAbsSender telegram) {
        ApiRequests.answerCallbackQuery(query, "Ну и ладно :(").callAsync(telegram);
        ApiRequests.editMessage(query,
                formatEditedMessage(query, "\uD83D\uDEAB \uD83C\uDF82 %s отказался от тортика %s")
        ).callAsync(telegram);
    }

    private void cakeIsRotten(CallbackQuery query, CommonAbsSender telegram) {
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
