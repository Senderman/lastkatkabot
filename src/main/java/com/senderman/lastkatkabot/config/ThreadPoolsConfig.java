package com.senderman.lastkatkabot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadPoolsConfig {

    @Bean
    public ScheduledExecutorService userActivityTrackerPool() {
        return Executors.newScheduledThreadPool(1);
    }

    @Bean
    public ScheduledExecutorService chatPolicyPool() {
        return Executors.newScheduledThreadPool(1);
    }

    @Bean
    public ExecutorService pairPool() {
        return Executors.newFixedThreadPool(1);
    }

    @Bean
    @Primary
    public ExecutorService generalNeedsPool() {
        int cpus = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(Math.max(2, cpus / 2));
    }
}
