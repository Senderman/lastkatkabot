package com.senderman.lastkatkabot.feature.roleplay.command;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.command.CallbackExecutor;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Singleton
public class CakeCallback implements CallbackExecutor {

    public final static String NAME = "CAKE";

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(CallbackQueryContext ctx) {
        var query = ctx.callbackQuery();
        if (!query.getFrom().getId().equals(Long.parseLong(ctx.argument(1)))) {
            ctx.answer("Этот тортик не вам!", true).callAsync(ctx.sender);
            return;
        }

        if (query.getMessage().getDate() + 2400 < System.currentTimeMillis() / 1000) {
            cakeIsRotten(ctx);
            return;
        }

        var action = ctx.argument(0);
        if (action.equals("accept"))
            acceptCake(ctx);
        else if (action.equals("decline"))
            declineCake(ctx);
    }

    private void acceptCake(CallbackQueryContext ctx) {
        ctx.answer("П p u я т н o г o  a п п e т u т a").callAsync(ctx.sender);
        ctx.editMessage(formatEditedMessage(ctx.callbackQuery(), "🎂 %s принял тортик %s"))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private void declineCake(CallbackQueryContext ctx) {
        ctx.answer("Ну и ладно :(").callAsync(ctx.sender);
        ctx.editMessage(formatEditedMessage(ctx.callbackQuery(), "🚫 🎂 %s отказался от тортика %s"))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private void cakeIsRotten(CallbackQueryContext ctx) {
        ctx.answer("Тортик испортился!", true).callAsync(ctx.sender);
        ctx.editMessage("🤢 Тортик попытались взять, но он испортился!")
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private String formatEditedMessage(CallbackQuery query, String format) {
        String cakeInsides = query.getMessage()
                .getText()
                .split("тортик", 2)[1]
                .strip();
        return format.formatted(query.getFrom().getFirstName(), cakeInsides);
    }
}
