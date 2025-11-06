package com.tariff.news.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for NewsEmbeddingService tests
 * Provides mock beans and test-specific configurations
 */
@TestConfiguration
public class NewsEmbeddingServiceTestConfig {

    @Bean
    @Primary
    public WebClient testWebClient() {
        return mock(WebClient.class);
    }

    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        return new ObjectMapper();
    }
}
