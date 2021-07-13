package com.senderman.lastkatkabot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.service.CachingUserActivityTrackerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class Beans2 {

    private final ChatUserService chatUserService;
    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;

    public Beans2(
            ChatUserService chatUserService,
            @Qualifier("jsonMapper") ObjectMapper jsonMapper,
            YAMLMapper yamlMapper
    ) {
        this.chatUserService = chatUserService;
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
    }

    @Bean
    public ScheduledExecutorService threadPool() {
        int cpus = Runtime.getRuntime().availableProcessors() - 1;
        return Executors.newScheduledThreadPool(Math.max(cpus, 1));
    }

    @Bean
    public Love love() {
        try {
            return yamlMapper.readValue(getClass().getResourceAsStream("/love.yml"), Love.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO implement CachingUserActivityTrackerService as CommandExecutor
    @Bean
    public CachingUserActivityTrackerService cachingUserActivityTrackerService() {
        var s = CachingUserActivityTrackerService.newInstance(chatUserService);
        s.runCacheListener();
        return s;
    }

}
