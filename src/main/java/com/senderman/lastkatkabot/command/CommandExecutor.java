package com.senderman.lastkatkabot.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.service.TriggerHandler;

/**
 * Subclasses of this interface should process messages with commands
 */
public interface CommandExecutor extends TriggerHandler<MessageContext> {

    String getDescription();

    default boolean showInHelp() {
        return true;
    }

}
