package com.sadna_market.market.InfrastructureLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Value("${market.admin.username:admin}")
    private String adminUsername;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Enable Java 8 date/time modules
        return mapper;
    }

    @Bean
    public String adminUsername() {
        return adminUsername;
    }
}