package com.senderman.lastkatkabot.command;

import com.annimon.tgbotsmodule.commands.TextCommand;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * Subclasses of this interface should process messages with commands
 */

public interface CommandExecutor extends TextCommand {

    String getDescription();

    default boolean showInHelp() {
        return true;
    }

    @Override
    default EnumSet<Role> authority() {
        return EnumSet.of(Role.USER);
    }

    void accept(@NotNull L10nMessageContext context);

    @Override
    default void accept(@NotNull MessageContext context) {
        accept((L10nMessageContext) context);
    }
}
