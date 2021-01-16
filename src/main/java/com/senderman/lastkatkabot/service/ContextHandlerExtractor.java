package com.senderman.lastkatkabot.service;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default HandlerExtractor implementation that can be used to get HandlerExecutor by command, using spring context
 */
@Component
public class ContextHandlerExtractor<T extends TriggerHandler<?>> implements HandlerExtractor<T> {

    private final Map<String, T> executors;

    @Autowired
    public ContextHandlerExtractor(Set<T> executors) {
        this.executors = executors.stream().collect(Collectors.toMap(T::getTrigger, Function.identity()));
    }

    @Override
    @Nullable
    public Optional<T> findHandler(String trigger) {
        return Optional.ofNullable(executors.get(trigger));
    }
}
