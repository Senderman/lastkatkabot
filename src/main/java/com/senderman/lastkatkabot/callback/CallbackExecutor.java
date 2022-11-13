package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.commands.CallbackQueryCommand;
import com.senderman.lastkatkabot.Role;

import java.util.EnumSet;

public abstract class CallbackExecutor implements CallbackQueryCommand {

    private String command;
    private EnumSet<Role> authority;

    public CallbackExecutor() {
    }

    public CallbackExecutor(String command, EnumSet<Role> authority) {
        this.command = command;
        this.authority = authority;
    }

    @Override
    public String command() {
        return command;
    }

    @Override
    public EnumSet<Role> authority() {
        return authority;
    }
}
