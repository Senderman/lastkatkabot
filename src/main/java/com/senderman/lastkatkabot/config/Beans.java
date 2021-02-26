package com.senderman.lastkatkabot.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class Beans {

    private final ChatUserService chatUserService;

    public Beans(ChatUserService chatUserService) {
        this.chatUserService = chatUserService;
    }


    @Bean
    public ScheduledExecutorService threadPool() {
        int cpus = Runtime.getRuntime().availableProcessors() - 1;
        return Executors.newScheduledThreadPool(Math.max(cpus, 1));
    }

    @Bean
    public Love love() {
        try {
            return new YAMLMapper().readValue(getClass().getResourceAsStream("/love.yml"), Love.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    /*@Bean
    public UserActivityTrackerService activityTrackerService() {
        return CachingUserActivityTrackerService.newInstance(chatUserService, threadPool());
    }*/

}
