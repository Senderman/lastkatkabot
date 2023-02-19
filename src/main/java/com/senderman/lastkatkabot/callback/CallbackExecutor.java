package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.commands.CallbackQueryCommand;
import com.senderman.lastkatkabot.Role;

import java.util.EnumSet;

public interface CallbackExecutor extends CallbackQueryCommand {

    @Override
    default EnumSet<Role> authority() {
        return EnumSet.of(Role.USER);
    }
}
