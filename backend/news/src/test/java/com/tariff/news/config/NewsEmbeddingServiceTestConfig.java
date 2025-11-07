package com.tariff.news.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;

/**
 * Test-only Spring configuration for NewsEmbeddingService-related tests.
 * - Provides @Primary beans so tests can inject predictable collaborators.
 * - WebClient is a Mockito mock to avoid real network I/O in unit/integration tests.
 * - ObjectMapper is a real instance for stable JSON parsing/serialization in tests.
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
