package com.senderman.lastkatkabot.feature.l10n.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.Locale;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Arrays;

@Command
public class SetLocaleCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/setlocale";
    }

    @Override
    public String getDescriptionKey() {
        return "localization.setlocale.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var buttonRows = Arrays.stream(Locale.values())
                .map(l -> new InlineKeyboardRow(ButtonBuilder.callbackButton(l.getName())
                        .payload(LocaleCallback.NAME, l.getCode())
                        .create()))
                .toList();
        ctx.reply(ctx.getString("localization.setlocale.message"))
                .setInlineKeyboard(buttonRows)
                .callAsync(ctx.sender);
    }

}

