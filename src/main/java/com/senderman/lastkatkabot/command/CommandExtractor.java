package com.senderman.lastkatkabot.command;

import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;


public interface CommandExtractor {

    /**
     * Find executor by command
     *
     * @param command command starting with "/"
     * @return appropriate command executor implementation, or null if command not found
     */
    @Nullable
    public CommandExecutor findExecutor(String command);

}
