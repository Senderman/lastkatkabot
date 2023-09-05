package com.senderman.lastkatkabot.feature.cake.command;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.cake.model.Cake;
import com.senderman.lastkatkabot.feature.cake.service.CakeService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Objects;

@Singleton
public class CakeCallback implements CallbackExecutor {

    public static final String NAME = "CAKE";
    private static final int CAKE_TIMEOUT_SECONDS = 2400;

    private final CakeService cakeService;

    public CakeCallback(CakeService cakeService) {
        this.cakeService = cakeService;
    }

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

        int cakeId = Integer.parseInt(ctx.argument(2));
        var cakeOptional = cakeService.findById(cakeId);

        // we can safely delete cake from db from now one, because it is to be rotten, accepted or declined
        cakeService.deleteById(cakeId);

        // if no cake in database or it's too late
        if (cakeOptional.isEmpty() || query.getMessage().getDate() + CAKE_TIMEOUT_SECONDS < System.currentTimeMillis() / 1000) {
            cakeIsRotten(ctx);
            return;
        }

        var action = ctx.argument(0);
        if (action.equals("accept"))
            acceptCake(ctx, cakeOptional.get());
        else if (action.equals("decline"))
            declineCake(ctx, cakeOptional.get());
    }

    private void acceptCake(L10nCallbackQueryContext ctx, Cake cake) {
        ctx.answer(ctx.getString("roleplay.cake.acceptNotify")).callAsync(ctx.sender);
        ctx.editMessage(formatEditedMessage(cake, ctx.callbackQuery(), ctx.getString("roleplay.cake.acceptMessage")))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private void declineCake(L10nCallbackQueryContext ctx, Cake cake) {
        ctx.answer(ctx.getString("roleplay.cake.declineNotify")).callAsync(ctx.sender);
        ctx.editMessage(formatEditedMessage(cake, ctx.callbackQuery(), ctx.getString("roleplay.cake.declineMessage")))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private void cakeIsRotten(L10nCallbackQueryContext ctx) {
        ctx.answer(ctx.getString("roleplay.cake.rottenNotify"), true).callAsync(ctx.sender);
        ctx.editMessage(ctx.getString("roleplay.cake.rottenMessage"))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private String formatEditedMessage(Cake cake, CallbackQuery query, String format) {
        String cakeFiller = Objects.requireNonNullElse(cake.getFilling(), "");
        return format.formatted(query.getFrom().getFirstName(), cakeFiller);
    }
}
