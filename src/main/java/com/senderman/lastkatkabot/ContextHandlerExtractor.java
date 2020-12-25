package com.senderman.lastkatkabot;

import com.senderman.lastkatkabot.command.HandlerExtractor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default HandlerExtractor implementation that can be used to get HandlerExecutor by command, using spring context
 * To make things work, annotate each TriggerHandler interface implementation as @Component(trigger)
 * For example: @Component("/help") class Help{...} or @Component("CB_SHOW_INFO") class CallbackInfo{...}
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
    public T findExecutor(String trigger) {
        return executors.get(trigger);
    }
}
