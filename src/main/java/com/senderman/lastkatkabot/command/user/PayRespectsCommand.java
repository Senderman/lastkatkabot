package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;

@Command(
        command = "/f",
        description = "(reply) press f to pay respects. А можно вот так: /f штаны за 40 хривень. Или просто /f"
)
public class PayRespectsCommand extends CommandExecutor {

    @Override
    public void accept(MessageContext ctx) {
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
                "\n" + ctx.user().getFirstName() + " has paid respects";

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("F")
                        .payload(Callbacks.F))
                .build();

        ctx.reply(text).setReplyMarkup(markup).callAsync(ctx.sender);
    }
}
