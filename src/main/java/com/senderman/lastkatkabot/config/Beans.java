package com.senderman.lastkatkabot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.senderman.lastkatkabot.Love;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import com.senderman.lastkatkabot.service.UserActivityTrackerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class Beans {

    private final ChatUserRepository chatUserRepo;

    public Beans(ChatUserRepository chatUserRepo) {
        this.chatUserRepo = chatUserRepo;
    }

    @Bean
    public ObjectMapper ymlMapper() {
        return new YAMLMapper();
    }

    @Bean
    public ExecutorService threadPool() {
        int cpus = Runtime.getRuntime().availableProcessors() - 1;
        return Executors.newFixedThreadPool(Math.max(cpus, 1));
    }

    @Bean
    public Love love() {
        try {
            return ymlMapper().readValue(getClass().getResourceAsStream("/love.yml"), Love.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public UserActivityTrackerService activityTrackerService() {
        return UserActivityTrackerService.newInstance(chatUserRepo);
    }

}
