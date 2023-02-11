package com.senderman.lastkatkabot.config;

import com.senderman.lastkatkabot.util.Threads;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Factory
public class ThreadPoolsConfig {

    @Singleton
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(2, new Threads.NamedThreadFactory("schedulerPool-%d"));
    }

    /*
    /pair only
     */
    @Singleton
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
    public ExecutorService generalNeedsPool() {
        int cpus = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(Math.max(2, cpus / 2), new Threads.NamedThreadFactory("generalNeedsPool-%d"));
    }
}
