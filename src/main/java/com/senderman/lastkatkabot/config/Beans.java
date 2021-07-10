package com.senderman.lastkatkabot.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.service.CachingUserActivityTrackerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class Beans {

    private final ChatUserService chatUserService;
    private ScheduledExecutorService threadPool;

    public Beans(ChatUserService chatUserService) {
        this.chatUserService = chatUserService;
    }


    @Bean
    public ScheduledExecutorService threadPool() {
        if (this.threadPool != null) return this.threadPool;
        int cpus = Runtime.getRuntime().availableProcessors() - 1;
        this.threadPool = Executors.newScheduledThreadPool(Math.max(cpus, 1));
        return this.threadPool;
    }

    @Bean
    public Love love() {
        try {
            return new YAMLMapper().readValue(getClass().getResourceAsStream("/love.yml"), Love.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    // TODO implement CachingUserActivityTrackerService as CommandExecutor
    @Bean
    public CachingUserActivityTrackerService cachingUserActivityTrackerService() {
        var s =CachingUserActivityTrackerService.newInstance(chatUserService, threadPool);
        //s.runCacheListener();
        return s;
    }

}
