package com.senderman.lastkatkabot.config;

import com.senderman.lastkatkabot.util.Threads;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.scheduling.ScheduledExecutorTaskScheduler;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Factory
public class ThreadPoolsConfig {

    @Singleton
    @Replaces(TaskScheduler.class)
    @Named("taskScheduler")
    public TaskScheduler taskScheduler() {
        var executor = Executors.newScheduledThreadPool(2, new Threads.NamedThreadFactory("schedulerPool-%d"));
        return new ScheduledExecutorTaskScheduler(executor);

    }

    /*
    /pair only
     */
    @Singleton
    @Named("pairPool")
    public ExecutorService pairPool() {
        return Executors.newFixedThreadPool(1, new Threads.NamedThreadFactory("pairPool-%d"));
    }

    /*
    /wru
    /weather
    new chat member processing
    image generation
    calling synchronized methods
     */
    @Singleton
    @Named("generalNeedsPool")
    public ExecutorService generalNeedsPool() {
        int cpus = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(Math.max(2, cpus / 2), new Threads.NamedThreadFactory("generalNeedsPool-%d"));
    }
}
