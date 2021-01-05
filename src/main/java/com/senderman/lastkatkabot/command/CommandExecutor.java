package com.senderman.lastkatkabot.command;

import com.senderman.lastkatkabot.TriggerHandler;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Subclasses of this interface should process messages with commands
 */
public interface CommandExecutor extends TriggerHandler<Message> {

    String getDescription();

    default boolean showInHelp() {
        return true;
    }

}
