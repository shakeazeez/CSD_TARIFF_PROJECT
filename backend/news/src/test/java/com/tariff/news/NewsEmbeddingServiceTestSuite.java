package com.tariff.news;

import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test suite for all NewsEmbeddingService tests
 * This class runs all tests in the news package
 */
@Suite
@SelectPackages({"com.tariff.news.service", "com.tariff.news.article"})
@SpringBootTest
@ActiveProfiles("test")
public class NewsEmbeddingServiceTestSuite {

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads properly for testing
        // All other tests are run via the @SelectPackages annotation
    }
}
