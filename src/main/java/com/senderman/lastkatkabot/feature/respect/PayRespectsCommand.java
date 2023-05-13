package com.senderman.lastkatkabot.feature.respect;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;

@Command
public class PayRespectsCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/f";
    }

    @Override
    public String getDescription() {
        return "respect.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        ctx.setArgumentsLimit(1);
        String object;

        if (ctx.argumentsLength() > 0) { // when object defined in the message text
            object = "to " + ctx.argument(0);
        } else if (ctx.message().isReply()) { // when object is another user
            object = "to " + ctx.message().getReplyToMessage().getFrom().getFirstName();
        } else {
            object = "";
        }

        ctx.deleteMessage().callAsync(ctx.sender);
        var text = "🕯 Press F to pay respects " + object +
                "\n" + Html.htmlSafe(ctx.user().getFirstName()) + " has paid respects";

        ctx.reply(text)
                .setInlineKeyboard(ButtonBuilder.callbackButton()
                        .text("F")
                        .payload(PayRespectsCallback.NAME)
                        .create())
                .callAsync(ctx.sender);
    }
}
