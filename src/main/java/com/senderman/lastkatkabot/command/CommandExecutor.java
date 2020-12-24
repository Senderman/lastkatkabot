package com.senderman.lastkatkabot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandExecutor {

    default String getCommand(){
        return getMyCommand(this);
    }

    String getDescription();

    default boolean isMainAdminOnly(){
        return false;
    }

    default boolean isAdminOnly(){
        return false;
    }

    default boolean showInHelp(){
        return true;
    }

    void execute(Message message);


    private String getMyCommand(CommandExecutor executor) {
        return executor.getClass().getAnnotation(Component.class).value();
    }
}
