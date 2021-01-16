package com.senderman.lastkatkabot.service;

import java.util.Optional;

public interface HandlerExtractor<T extends TriggerHandler<?>> {

    /**
     * Find executor by command
     *
     * @param trigger command starting with "/"
     * @return appropriate command executor implementation, or empty optional
     */

    Optional<T> findHandler(String trigger);

}
