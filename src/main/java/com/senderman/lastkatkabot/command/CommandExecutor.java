package com.senderman.lastkatkabot.command;

import com.annimon.tgbotsmodule.commands.TextCommand;
import com.senderman.lastkatkabot.Role;

import java.util.EnumSet;
import java.util.Set;

/**
 * Subclasses of this interface should process messages with commands
 */

public abstract class CommandExecutor implements TextCommand {

    private String command;
    private String description;
    private boolean showInHelp;
    private EnumSet<Role> authority;
    private Set<String> aliases;

    public CommandExecutor() {
    }

    public CommandExecutor(String command, String description, boolean showInHelp, EnumSet<Role> authority, Set<String> aliases) {
        this.command = command;
        this.description = description;
        this.showInHelp = showInHelp;
        this.authority = authority;
        this.aliases = aliases;
    }

    @Override
    public String command() {
        return command;
    }

    @Override
    public EnumSet<Role> authority() {
        return authority;
    }

    @Override
    public Set<String> aliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public boolean showInHelp() {
        return showInHelp;
    }

}
