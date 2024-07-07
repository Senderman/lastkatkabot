package com.senderman.lastkatkabot.command;

import com.annimon.tgbotsmodule.commands.TextCommand;
import com.annimon.tgbotsmodule.commands.context.Context;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * Subclasses of this interface should process messages with commands
 */

public interface CommandExecutor extends TextCommand {

    /**
     * Key in locale files with this command's description
     *
     * @return key in locale files with this command's description
     */
    String getDescriptionKey();

    /**
     * Decides if this command should be shown in /help command
     * @return true if this command should be shown in /help command, otherwise false
     */
    default boolean showInHelp() {
        return true;
    }

    /**
     * List of roles that will be able to use this command and see this command in /help command output.
     * Note that if this command is allowed to use by some role, any user with role with higher access rights
     * will be also able to use it.
     * @return {@link EnumSet} of roles allowed to use this command
     */
    @Override
    default EnumSet<Role> authority() {
        return EnumSet.of(Role.USER);
    }

    /**
     * Wrapper for {@link com.annimon.tgbotsmodule.commands.Command#accept(Context)} method
     * with {@link MessageContext} wrapped with {@link L10nMessageContext} for convenience.
     * You can define your command's logic here
     *
     * @param ctx {@link MessageContext} wrapped with {@link L10nMessageContext}.
     */
    void accept(@NotNull L10nMessageContext ctx);

    @Override
    default void accept(@NotNull MessageContext ctx) {
        accept((L10nMessageContext) ctx);
    }
}
