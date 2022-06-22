package com.senderman.lastkatkabot.config;

import com.senderman.lastkatkabot.util.NamedThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadPoolsConfig {

    @Bean
    public ScheduledExecutorService userActivityTrackerPool() {
        return Executors.newScheduledThreadPool(1, new NamedThreadFactory("userActivityTrackerPool-%d"));
    }

    @Bean
    public ScheduledExecutorService chatPolicyPool() {
        return Executors.newScheduledThreadPool(1, new NamedThreadFactory("chatPolicyPool-%d"));
    }

    @Bean
    public ExecutorService pairPool() {
        return Executors.newFixedThreadPool(1, new NamedThreadFactory("pairPool-%d"));
    }

    @Bean
    public ExecutorService generalNeedsPool() {
        int cpus = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(Math.max(2, cpus / 2), new NamedThreadFactory("generalNeedsPool-%d"));
    }
}
