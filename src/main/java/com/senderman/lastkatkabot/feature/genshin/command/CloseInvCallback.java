package com.senderman.lastkatkabot.feature.genshin.command;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedCallbackQueryContext;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class CloseInvCallback implements CallbackExecutor {

    public static final String NAME = "GCLOSE";

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(@NotNull LocalizedCallbackQueryContext ctx) {
        final var userId = ctx.user().getId();
        final var ownerId = Long.parseLong(ctx.argument(0));

        if (!userId.equals(ownerId)) {
            ctx.answerAsAlert(ctx.getString("genshin.inv.notYourInv")).callAsync(ctx.sender);
            return;
        }
        ctx.editMessage(ctx.getString("genshin.inv.deletedInv").formatted(Html.htmlSafe(ctx.user().getFirstName())))
                .callAsync(ctx.sender);
        ctx.answer(ctx.getString("genshin.inv.invClosedNotify")).callAsync(ctx.sender);
    }
}
