package com.senderman.lastkatkabot.command;

import com.annimon.tgbotsmodule.commands.TextCommand;
import com.senderman.lastkatkabot.Role;

import java.util.EnumSet;

/**
 * Subclasses of this interface should process messages with commands
 */
public interface CommandExecutor extends TextCommand {

    @Override
    default EnumSet<Role> authority() {
        return EnumSet.of(Role.USER);
    }

    String getDescription();

    default boolean showInHelp() {
        return true;
    }

}
