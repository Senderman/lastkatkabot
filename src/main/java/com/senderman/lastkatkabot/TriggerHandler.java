package com.senderman.lastkatkabot;

import org.springframework.stereotype.Component;

public interface TriggerHandler<TTrigger> {

    default String getTrigger() {
        return getMyName(this);
    }

    void execute(TTrigger trigger);

    private String getMyName(TriggerHandler<TTrigger> executor) {
        return executor.getClass().getAnnotation(Component.class).value();
    }

}
