package com.senderman.lastkatkabot.service;

import com.annimon.tgbotsmodule.commands.context.Context;
import com.senderman.lastkatkabot.Role;

import java.util.EnumSet;

public interface TriggerHandler<TContext extends Context> {

    String getTrigger();

    default EnumSet<Role> getRoles() {
        return EnumSet.of(Role.USER);
    }

    void execute(TContext ctx);

}
