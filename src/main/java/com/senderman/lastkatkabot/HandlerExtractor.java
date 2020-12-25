package com.senderman.lastkatkabot;

import org.jetbrains.annotations.Nullable;

public interface HandlerExtractor<T extends TriggerHandler<?>> {

    /**
     * Find executor by command
     *
     * @param trigger command starting with "/"
     * @return appropriate command executor implementation, or null if command not found
     */

    @Nullable T findExecutor(String trigger);

}
