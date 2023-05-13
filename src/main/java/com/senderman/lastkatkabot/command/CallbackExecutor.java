package com.senderman.lastkatkabot.command;

import com.annimon.tgbotsmodule.commands.CallbackQueryCommand;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public interface CallbackExecutor extends CallbackQueryCommand {

    @Override
    default EnumSet<Role> authority() {
        return EnumSet.of(Role.USER);
    }

    void accept(@NotNull L10nCallbackQueryContext context);

    @Override
    default void accept(@NotNull CallbackQueryContext context) {
        accept((L10nCallbackQueryContext) context);
    }
}
