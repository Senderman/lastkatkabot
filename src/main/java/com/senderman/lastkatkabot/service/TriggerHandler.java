package com.senderman.lastkatkabot.service;

import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;

import java.util.EnumSet;

public interface TriggerHandler<TTrigger> {

    String getTrigger();

    default EnumSet<Role> getRoles() {
        return EnumSet.of(Role.USER);
    }

    void execute(TTrigger trigger, CommonAbsSender telegram);

}
