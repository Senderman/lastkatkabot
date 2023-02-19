package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;

@Singleton
public class PayRespectsCallback implements CallbackExecutor {

    @Override
    public String command() {
        return Callbacks.F;
    }

    @Override
    public void accept(CallbackQueryContext ctx) {

        if (ctx.message().getText().contains(ctx.user().getFirstName())) {
            ctx.answer("You've already payed respects! (or you've tried to pay respects to yourself)", true).callAsync(ctx.sender);
            return;
        }

        var message = ctx.message();
        ctx.answer("You've paid respects").callAsync(ctx.sender);
        ctx.editMessage(message.getText() + "\n" + Html.htmlSafe(ctx.user().getFirstName()) + " has paid respects")
                .setReplyMarkup(message.getReplyMarkup())
                .disableWebPagePreview()
                .callAsync(ctx.sender);

    }
}
