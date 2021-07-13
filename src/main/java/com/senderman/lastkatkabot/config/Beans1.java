package com.senderman.lastkatkabot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans1 {

    @Bean
    public ObjectMapper jsonMapper(){
        return new ObjectMapper();
    }

    @Bean
    public YAMLMapper yamlMapper(){
        return new YAMLMapper();
    }

}
