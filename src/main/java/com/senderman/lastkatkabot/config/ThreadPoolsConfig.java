package com.senderman.lastkatkabot.config;

import com.senderman.lastkatkabot.util.Threads;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class ThreadPoolsConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.initialize();
        return scheduler;
    }

    /*
    /pair only
     */
    @Bean
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
    @Bean
    public ExecutorService generalNeedsPool() {
        int cpus = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(Math.max(2, cpus / 2), new Threads.NamedThreadFactory("generalNeedsPool-%d"));
    }
}
