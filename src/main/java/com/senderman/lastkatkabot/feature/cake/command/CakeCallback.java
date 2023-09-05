package com.senderman.lastkatkabot.feature.cake.command;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
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
    public void accept(L10nCallbackQueryContext ctx) {
        var query = ctx.callbackQuery();
        if (!query.getFrom().getId().equals(Long.parseLong(ctx.argument(1)))) {
            ctx.answer(ctx.getString("roleplay.cake.notYourCake"), true).callAsync(ctx.sender);
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

    private void acceptCake(L10nCallbackQueryContext ctx) {
        ctx.answer(ctx.getString("roleplay.cake.acceptNotify")).callAsync(ctx.sender);
        ctx.editMessage(formatEditedMessage(ctx, ctx.callbackQuery(),
                        ctx.getString("roleplay.cake.acceptMessage")))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private void declineCake(L10nCallbackQueryContext ctx) {
        ctx.answer(ctx.getString("roleplay.cake.declineNotify")).callAsync(ctx.sender);
        ctx.editMessage(formatEditedMessage(ctx, ctx.callbackQuery(),
                        ctx.getString("roleplay.cake.declineMessage")))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private void cakeIsRotten(L10nCallbackQueryContext ctx) {
        ctx.answer(ctx.getString("roleplay.cake.rottenNotify"), true).callAsync(ctx.sender);
        ctx.editMessage(ctx.getString("roleplay.cake.rottenMessage"))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private String formatEditedMessage(L10nCallbackQueryContext ctx, CallbackQuery query, String format) {
        String cakeInsides = query.getMessage()
                .getText()
                .split(ctx.getString("roleplay.cake.cake"), 2)[1]
                .strip();
        return format.formatted(query.getFrom().getFirstName(), cakeInsides);
    }
}
