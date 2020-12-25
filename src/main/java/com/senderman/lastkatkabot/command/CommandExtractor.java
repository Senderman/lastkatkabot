package com.senderman.lastkatkabot.command;

import org.jetbrains.annotations.Nullable;

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
