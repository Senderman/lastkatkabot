package com.senderman.lastkatkabot.feature.genshin.command;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.command.CallbackExecutor;
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
    public void accept(@NotNull CallbackQueryContext ctx) {
        final var userId = ctx.user().getId();
        final var ownerId = Long.parseLong(ctx.argument(0));

        if (!userId.equals(ownerId)) {
            ctx.answerAsAlert("Это не ваш инвентарь!").callAsync(ctx.sender);
            return;
        }
        ctx.editMessage("Здесь был инвентарь, но его закрыл %s".formatted(Html.htmlSafe(ctx.user().getFirstName())))
                .callAsync(ctx.sender);
        ctx.answer("Инвентарь закрыт").callAsync(ctx.sender);
    }
}
