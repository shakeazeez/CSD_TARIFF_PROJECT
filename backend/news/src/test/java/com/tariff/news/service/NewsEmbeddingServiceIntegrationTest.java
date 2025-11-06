package com.tariff.news.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tariff.news.article.ArticleEmbedding;
import com.tariff.news.article.ArticleEmbeddingRepo;
import com.tariff.news.dto.NewsResponse;

/**
 * Integration tests for NewsEmbeddingService with minimal external API calls
 * These tests focus on the service behavior with realistic data flows
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=test-key",
    "thenewsapi.api.key=test-key",
    "news.similarity.threshold=0.75",
    "news.db.candidate.limit=20",
    "news.db.return.limit=3",
    "news.api.candidate.limit=8",
    "news.db.per_article_threshold=0.6",
    "news.queryEmbedding.store=false",
    "news.db.use_pgvector=false",
    "news.api.published_after=2020-01-01"
})
public class NewsEmbeddingServiceIntegrationTest {

    @MockBean
    private WebClient webClient;

    @MockBean
    private ArticleEmbeddingRepo articleEmbeddingRepo;

    private NewsEmbeddingService newsEmbeddingService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @MockBean
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @MockBean
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @MockBean
    private WebClient.RequestBodySpec requestBodySpec;

    @MockBean
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        newsEmbeddingService = new NewsEmbeddingService(webClient, objectMapper, articleEmbeddingRepo);
    }

    @Test
    void testFullWorkflow_DatabaseHit_EndToEnd() throws Exception {
        // Arrange
        String userQuery = "What is the impact of US-China trade tariffs on agriculture?";
        
        // Mock stored articles with high similarity
        List<ArticleEmbedding> storedArticles = createRealisticStoredArticles();
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(storedArticles);

        // Mock embedding generation
        setupEmbeddingMock(userQuery, createRealisticEmbedding());

        // Mock synthesis response
        setupSynthesisMock("Trade tariffs between the US and China have significantly impacted agricultural exports, with soybean farmers particularly affected by retaliatory measures.");

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(userQuery);

        // Assert
        assertNotNull(result);
        assertEquals("db", result.getSource());
        assertTrue(result.getSynthesizedAnswer().contains("tariffs"));
        assertTrue(result.getSynthesizedAnswer().contains("agricultural"));
        assertEquals(3, result.getArticles().size());

        // Verify we hit the database but didn't call external news API
        verify(articleEmbeddingRepo).findAllByEmbeddingIsNotNull();
        verify(webClient, never()).get(); // No news API calls
    }

    @Test
    void testFullWorkflow_DatabaseMiss_FallsBackToAPI() throws Exception {
        // Arrange
        String userQuery = "Latest developments in semiconductor trade policies";
        
        // Mock empty database or low similarity articles
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(new ArrayList<>());

        // Mock all necessary API calls
        setupEmbeddingMock(userQuery, createRealisticEmbedding());
        setupTopicExtractionMock(userQuery, "semiconductor trade");
        setupNewsApiMock();
        setupSynthesisMock("Recent semiconductor trade policies focus on supply chain security and domestic manufacturing incentives.");

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(userQuery);

        // Assert
        assertNotNull(result);
        assertEquals("api", result.getSource());
        assertTrue(result.getSynthesizedAnswer().contains("semiconductor"));
        assertFalse(result.getArticles().isEmpty());

        // Verify both database query and API fallback occurred
        verify(articleEmbeddingRepo).findAllByEmbeddingIsNotNull();
        verify(webClient, atLeastOnce()).get(); // News API calls
        verify(webClient, atLeastOnce()).post(); // OpenAI API calls
    }

    @Test
    void testPgVectorEnabledWorkflow() throws Exception {
        // Arrange
        String userQuery = "renewable energy trade agreements";
        
        // Create service with pgvector enabled
        NewsEmbeddingService pgVectorService = new NewsEmbeddingService(webClient, objectMapper, articleEmbeddingRepo);
        org.springframework.test.util.ReflectionTestUtils.setField(pgVectorService, "usePgvector", true);
        org.springframework.test.util.ReflectionTestUtils.setField(pgVectorService, "openaiApiKey", "test-key");
        org.springframework.test.util.ReflectionTestUtils.setField(pgVectorService, "similarityThreshold", 0.75);
        org.springframework.test.util.ReflectionTestUtils.setField(pgVectorService, "dbCandidateLimit", 20);
        org.springframework.test.util.ReflectionTestUtils.setField(pgVectorService, "dbReturnLimit", 3);

        // Mock pgvector database call
        List<ArticleEmbedding> pgVectorResults = createRealisticStoredArticles();
        when(articleEmbeddingRepo.findClosestArticles(anyString(), anyInt())).thenReturn(pgVectorResults);

        setupEmbeddingMock(userQuery, createRealisticEmbedding());
        setupSynthesisMock("Renewable energy trade agreements are promoting clean technology transfer and carbon border adjustments.");

        // Act
        NewsResponse result = pgVectorService.processQuery(userQuery);

        // Assert
        assertNotNull(result);
        assertEquals("db", result.getSource());
        assertTrue(result.getSynthesizedAnswer().contains("renewable"));

        // Verify pgvector method was called
        verify(articleEmbeddingRepo).findClosestArticles(anyString(), eq(20));
        verify(articleEmbeddingRepo, never()).findAllByEmbeddingIsNotNull();
    }

    @Test
    void testErrorHandling_DatabaseFailure() throws Exception {
        // Arrange
        String userQuery = "trade policy analysis";
        
        // Mock database failure
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull())
            .thenThrow(new RuntimeException("Database connection failed"));

        setupEmbeddingMock(userQuery, createRealisticEmbedding());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            newsEmbeddingService.processQuery(userQuery);
        });
    }

    @Test
    void testErrorHandling_OpenAIAPIFailure() throws Exception {
        // Arrange
        String userQuery = "economic analysis";
        
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(new ArrayList<>());

        // Mock OpenAI API failure
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(contains("openai"))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
            .thenReturn(Mono.error(new RuntimeException("OpenAI API unavailable")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            newsEmbeddingService.processQuery(userQuery);
        });
    }

    @Test
    void testPerformance_LargeResultSet() throws Exception {
        // Arrange
        String userQuery = "global trade trends";
        
        // Create large result set
        List<ArticleEmbedding> largeResultSet = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ArticleEmbedding article = createSingleArticle(i);
            largeResultSet.add(article);
        }
        
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(largeResultSet);
        setupEmbeddingMock(userQuery, createRealisticEmbedding());
        setupSynthesisMock("Global trade trends show increasing focus on sustainability and digital transformation.");

        long startTime = System.currentTimeMillis();

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(userQuery);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert
        assertNotNull(result);
        assertEquals("db", result.getSource());
        assertEquals(3, result.getArticles().size()); // Should limit to dbReturnLimit
        
        // Performance assertion - should complete within reasonable time
        assertTrue(executionTime < 5000, "Query should complete within 5 seconds, took: " + executionTime + "ms");
    }

    @Test
    void testConfigurationValues_AreAppliedCorrectly() throws Exception {
        // Arrange
        String userQuery = "test query";
        
        // Verify that configuration values are being used
        List<ArticleEmbedding> articles = createRealisticStoredArticles();
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(articles);
        
        setupEmbeddingMock(userQuery, createRealisticEmbedding());
        setupSynthesisMock("Test response");

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(userQuery);

        // Assert
        assertNotNull(result);
        // The configuration should limit results to dbReturnLimit (3)
        assertEquals(3, result.getArticles().size());
    }

    // Helper methods

    private List<ArticleEmbedding> createRealisticStoredArticles() {
        List<ArticleEmbedding> articles = new ArrayList<>();
        
        String[] titles = {
            "US-China Trade War Impact on Agricultural Exports",
            "Semiconductor Supply Chain Disruptions in Asia",
            "European Carbon Border Tax Implementation"
        };
        
        String[] urls = {
            "https://tradereport.com/agriculture-impact",
            "https://techeconomy.com/semiconductor-supply",
            "https://eurotrade.eu/carbon-tax"
        };
        
        String[] contexts = {
            "This article analyzes how trade tensions affect farming communities",
            "Discussion of technology trade restrictions and their economic effects", 
            "Coverage of new environmental trade policies in the EU"
        };

        for (int i = 0; i < titles.length; i++) {
            ArticleEmbedding article = new ArticleEmbedding();
            article.setId((long) (i + 1));
            article.setTitle(titles[i]);
            article.setUrl(urls[i]);
            article.setCleanedText("Detailed article content about " + titles[i]);
            article.setEmbedding(createRealisticFloatArray());
            article.setTopic("trade policy");
            article.setQueryContext(contexts[i]);
            article.setLastSeenQuery("trade impact analysis");
            articles.add(article);
        }
        
        return articles;
    }

    private ArticleEmbedding createSingleArticle(int index) {
        ArticleEmbedding article = new ArticleEmbedding();
        article.setId((long) index);
        article.setTitle("Trade Article " + index);
        article.setUrl("https://example.com/article" + index);
        article.setCleanedText("Content for article " + index);
        article.setEmbedding(createRealisticFloatArray());
        article.setTopic("trade");
        article.setQueryContext("Context for article " + index);
        article.setLastSeenQuery("trade query");
        return article;
    }

    private List<Double> createRealisticEmbedding() {
        // Create a realistic embedding vector (1536 dimensions for text-embedding-ada-002)
        List<Double> embedding = new ArrayList<>();
        Random random = new Random(42); // Fixed seed for reproducible tests
        
        for (int i = 0; i < 1536; i++) {
            // Generate values in typical embedding range (-1 to 1, but usually smaller)
            embedding.add((random.nextGaussian() * 0.1));
        }
        
        return embedding;
    }

    private float[] createRealisticFloatArray() {
        float[] array = new float[1536];
        Random random = new Random(42);
        
        for (int i = 0; i < array.length; i++) {
            array[i] = (float) (random.nextGaussian() * 0.1);
        }
        
        return array;
    }

    private void setupEmbeddingMock(String text, List<Double> embedding) {
        try {
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("https://api.openai.com/v1/embeddings")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(
                "{\"data\":[{\"embedding\":" + embedding.toString().replace(" ", "") + "}]}"
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupTopicExtractionMock(String query, String topic) {
        try {
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any()));
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(
                "{\"choices\":[{\"message\":{\"content\":\"" + topic + "\"}}]}"
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupSynthesisMock(String answer) {
        try {
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any()));
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(
                "{\"choices\":[{\"message\":{\"content\":\"" + answer + "\"}}]}"
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setupNewsApiMock() {
        try {
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(
                "{\"data\":[" +
                "{\"title\":\"Latest Trade Policy Update\",\"url\":\"https://news.com/trade1\"}," +
                "{\"title\":\"Economic Impact Analysis\",\"url\":\"https://news.com/trade2\"}" +
                "]}"
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
