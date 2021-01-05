package com.senderman.lastkatkabot;

import java.util.EnumSet;

public interface TriggerHandler<TTrigger> {

    String getTrigger();

    default EnumSet<Role> getRoles() {
        return EnumSet.of(Role.USER);
    }

    void execute(TTrigger trigger);

}
