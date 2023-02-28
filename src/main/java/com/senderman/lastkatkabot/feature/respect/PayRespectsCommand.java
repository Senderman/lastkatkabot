package com.senderman.lastkatkabot.feature.respect;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Callbacks;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import jakarta.inject.Singleton;

@Singleton
@Command
public class PayRespectsCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/f";
    }

    @Override
    public String getDescription() {
        return "(reply) press f to pay respects. А можно вот так: /f штаны за 40 хривень. Или просто /f";
    }

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

        ctx.reply(text)
                .setInlineKeyboard(ButtonBuilder.callbackButton()
                        .text("F")
                        .payload(Callbacks.F)
                        .create())
                .callAsync(ctx.sender);
    }
}
