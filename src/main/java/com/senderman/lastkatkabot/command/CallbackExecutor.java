package com.senderman.lastkatkabot.command;

import com.annimon.tgbotsmodule.commands.CallbackQueryCommand;
import com.senderman.lastkatkabot.Role;

import java.util.EnumSet;

public interface CallbackExecutor extends CallbackQueryCommand {

    @Override
    default EnumSet<Role> authority() {
        return EnumSet.of(Role.USER);
    }
}
