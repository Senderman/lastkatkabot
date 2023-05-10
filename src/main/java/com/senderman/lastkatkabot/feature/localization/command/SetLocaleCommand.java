package com.senderman.lastkatkabot.feature.localization.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.localization.Locale;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Command
public class SetLocaleCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/setlocale";
    }

    @Override
    public String getDescription() {
        return "localization.setlocale.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        ctx.reply(ctx.getString("localization.setlocale.message"))
                .setInlineKeyboard(List.of(Arrays.stream(Locale.values()).map(l -> ButtonBuilder.callbackButton()
                        .text(l.getName())
                        .payload(LocaleCallback.NAME, l.getCode())
                        .create()).toList()))
                .callAsync(ctx.sender);
    }

}

