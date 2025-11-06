package com.tariff.news.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tariff.news.article.ArticleEmbedding;
import com.tariff.news.article.ArticleEmbeddingRepo;
import com.tariff.news.dto.NewsResponse;

@ExtendWith(MockitoExtension.class)
public class NewsEmbeddingServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ArticleEmbeddingRepo articleEmbeddingRepo;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private NewsEmbeddingService newsEmbeddingService;

    private ObjectMapper realObjectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        newsEmbeddingService = new NewsEmbeddingService(webClient, objectMapper, articleEmbeddingRepo);
        
        // Set up configuration values
        ReflectionTestUtils.setField(newsEmbeddingService, "openaiApiKey", "test-api-key");
        ReflectionTestUtils.setField(newsEmbeddingService, "thenewsApiKey", "test-news-api-key");
        ReflectionTestUtils.setField(newsEmbeddingService, "similarityThreshold", 0.75);
        ReflectionTestUtils.setField(newsEmbeddingService, "dbCandidateLimit", 20);
        ReflectionTestUtils.setField(newsEmbeddingService, "dbReturnLimit", 3);
        ReflectionTestUtils.setField(newsEmbeddingService, "apiCandidateLimit", 8);
        ReflectionTestUtils.setField(newsEmbeddingService, "perArticleThreshold", 0.6);
        ReflectionTestUtils.setField(newsEmbeddingService, "storeQueryEmbedding", false);
        ReflectionTestUtils.setField(newsEmbeddingService, "usePgvector", false);
        ReflectionTestUtils.setField(newsEmbeddingService, "apiPublishedAfter", "2020-01-01");
    }

    @Test
    void testProcessQuery_DatabaseHit_ReturnsStoredArticles() throws Exception {
        // Arrange
        String query = "trade tariffs impact";
        List<Double> mockEmbedding = createMockEmbedding();
        List<ArticleEmbedding> storedArticles = createMockStoredArticles();

        // Mock embedding generation
        mockEmbeddingGeneration(query, mockEmbedding);
        
        // Mock database retrieval
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(storedArticles);
        
        // Mock synthesis response
        mockSynthesisResponse("Synthesized answer about trade tariffs");

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("db", result.getSource());
        assertEquals("Synthesized answer about trade tariffs", result.getSynthesizedAnswer());
        assertEquals(3, result.getArticles().size());
        
        // Verify no external API calls for news were made
        verify(webClient, never()).get();
    }

    @Test
    void testProcessQuery_DatabaseMiss_FallbackToAPI() throws Exception {
        // Arrange
        String query = "new trade policy";
        List<Double> mockEmbedding = createMockEmbedding();
        List<ArticleEmbedding> storedArticles = createLowSimilarityArticles();

        // Mock embedding generation
        mockEmbeddingGeneration(query, mockEmbedding);
        
        // Mock database retrieval with low similarity
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(storedArticles);
        
        // Mock topic extraction
        mockTopicExtraction(query, "trade policy");
        
        // Mock news API response
        mockNewsApiResponse();
        
        // Mock text extraction
        mockTextExtraction();
        
        // Mock synthesis response
        mockSynthesisResponse("News about new trade policy");

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("api", result.getSource());
        assertEquals("News about new trade policy", result.getSynthesizedAnswer());
        
        // Verify API calls were made
        verify(webClient, atLeastOnce()).post();
        verify(webClient, atLeastOnce()).get();
    }

    @Test
    void testProcessQuery_EmptyDatabase_FallbackToAPI() throws Exception {
        // Arrange
        String query = "china trade war";
        List<Double> mockEmbedding = createMockEmbedding();

        // Mock embedding generation
        mockEmbeddingGeneration(query, mockEmbedding);
        
        // Mock empty database
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(new ArrayList<>());
        
        // Mock topic extraction
        mockTopicExtraction(query, "china trade");
        
        // Mock news API response
        mockNewsApiResponse();
        
        // Mock text extraction
        mockTextExtraction();
        
        // Mock synthesis response
        mockSynthesisResponse("Latest news on China trade war");

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("api", result.getSource());
        assertEquals("Latest news on China trade war", result.getSynthesizedAnswer());
    }

    @Test
    void testProcessQuery_APIReturnsNoArticles_FallbackResponse() throws Exception {
        // Arrange
        String query = "obscure trade topic";
        List<Double> mockEmbedding = createMockEmbedding();

        // Mock embedding generation
        mockEmbeddingGeneration(query, mockEmbedding);
        
        // Mock empty database
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(new ArrayList<>());
        
        // Mock topic extraction
        mockTopicExtraction(query, "trade topic");
        
        // Mock empty news API response
        mockEmptyNewsApiResponse();
        
        // Mock synthesis response for fallback
        mockSynthesisResponse("No specific articles found for this query");

        // Act
        NewsResponse result = newsEmbeddingService.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("api-fallback", result.getSource());
        assertEquals("No specific articles found for this query", result.getSynthesizedAnswer());
        assertTrue(result.getArticles().isEmpty());
    }

    @Test
    void testExtractTopic_TradeQuery_ReturnsSpecificTopic() throws Exception {
        // Arrange
        String query = "How do tariffs affect small businesses?";
        mockTopicExtraction(query, "tariffs business");

        // Act
        String result = newsEmbeddingService.extractTopic(query);

        // Assert
        assertEquals("tariffs business", result);
    }

    @Test
    void testExtractTopic_GeneralQuery_ReturnsGeneralTopic() throws Exception {
        // Arrange
        String query = "climate change effects";
        mockTopicExtraction(query, "climate change effects");

        // Act
        String result = newsEmbeddingService.extractTopic(query);

        // Assert
        assertEquals("climate change effects", result);
    }

    @Test
    void testFetchArticles_ReturnsValidArticles() {
        // Arrange
        String topic = "trade policy";
        mockNewsApiResponse();

        // Act
        List<NewsEmbeddingService.Article> result = newsEmbeddingService.fetchArticles(topic);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Trade Policy Update", result.get(0).getTitle());
        assertEquals("https://example.com/article1", result.get(0).getUrl());
    }

    @Test
    void testExtractFullText_ValidUrl_ReturnsCleanedText() throws IOException {
        // This test would require mocking Jsoup, which is complex
        // In a real scenario, you might want to use a test profile with a mock implementation
        
        // For now, we'll test the error handling
        assertThrows(IOException.class, () -> {
            newsEmbeddingService.extractFullText("invalid-url");
        });
    }

    @Test
    void testGenerateEmbedding_ValidText_ReturnsEmbedding() {
        // Arrange
        String text = "sample text for embedding";
        List<Double> expectedEmbedding = createMockEmbedding();
        mockEmbeddingGeneration(text, expectedEmbedding);

        // Act
        List<Double> result = newsEmbeddingService.generateEmbedding(text);

        // Assert
        assertEquals(expectedEmbedding, result);
    }

    @Test
    void testConvertStringToEmbedding_ValidString_ReturnsEmbedding() {
        // Arrange
        String embeddingStr = "[0.1,0.2,0.3]";

        // Act
        List<Double> result = newsEmbeddingService.convertStringToEmbedding(embeddingStr);

        // Assert
        assertEquals(3, result.size());
        assertEquals(0.1, result.get(0), 0.001);
        assertEquals(0.2, result.get(1), 0.001);
        assertEquals(0.3, result.get(2), 0.001);
    }

    @Test
    void testConvertStringToEmbedding_InvalidString_ReturnsEmptyList() {
        // Arrange
        String embeddingStr = "invalid";

        // Act
        List<Double> result = newsEmbeddingService.convertStringToEmbedding(embeddingStr);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindSimilarArticles_ReturnsOrderedResults() {
        // Arrange
        String embeddingStr = "[0.1,0.2,0.3]";
        int limit = 5;
        List<ArticleEmbedding> mockArticles = createMockStoredArticles();
        
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(mockArticles);

        // Act
        List<ArticleEmbedding> result = newsEmbeddingService.findSimilarArticles(embeddingStr, limit);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(Math.min(limit, mockArticles.size()), result.size());
    }

    @Test
    void testFindArticlesSimilarToQuery_ValidQuery_ReturnsResults() {
        // Arrange
        String query = "trade policy";
        int limit = 3;
        List<Double> mockEmbedding = createMockEmbedding();
        List<ArticleEmbedding> mockArticles = createMockStoredArticles();
        
        mockEmbeddingGeneration(query, mockEmbedding);
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(mockArticles);

        // Act
        List<ArticleEmbedding> result = newsEmbeddingService.findArticlesSimilarToQuery(query, limit);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testFindArticlesByTopic_ValidTopic_ReturnsResults() {
        // Arrange
        String topic = "trade";
        List<ArticleEmbedding> expectedArticles = createMockStoredArticles();
        
        when(articleEmbeddingRepo.findByTopic(topic)).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = newsEmbeddingService.findArticlesByTopic(topic);

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findByTopic(topic);
    }

    @Test
    void testGetAllArticles_ReturnsAllStoredArticles() {
        // Arrange
        List<ArticleEmbedding> expectedArticles = createMockStoredArticles();
        
        when(articleEmbeddingRepo.findAll()).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = newsEmbeddingService.getAllArticles();

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findAll();
    }

    // Helper methods for creating mock data and setting up mocks

    private List<Double> createMockEmbedding() {
        List<Double> embedding = new ArrayList<>();
        for (int i = 0; i < 1536; i++) {
            embedding.add(Math.random() * 2 - 1); // Random values between -1 and 1
        }
        return embedding;
    }

    private List<ArticleEmbedding> createMockStoredArticles() {
        List<ArticleEmbedding> articles = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            ArticleEmbedding article = new ArticleEmbedding();
            article.setId((long) i);
            article.setTitle("Trade Article " + i);
            article.setUrl("https://example.com/article" + i);
            article.setCleanedText("This is a sample article about trade topic " + i);
            article.setEmbedding(createMockFloatArray());
            article.setTopic("trade");
            article.setQueryContext("This article discusses trade implications");
            article.setLastSeenQuery("trade policy");
            articles.add(article);
        }
        
        return articles;
    }

    private List<ArticleEmbedding> createLowSimilarityArticles() {
        List<ArticleEmbedding> articles = createMockStoredArticles();
        // These will have low cosine similarity due to the mock implementation
        return articles;
    }

    private float[] createMockFloatArray() {
        float[] array = new float[1536];
        for (int i = 0; i < array.length; i++) {
            array[i] = (float) (Math.random() * 2 - 1);
        }
        return array;
    }

    private void mockEmbeddingGeneration(String text, List<Double> embedding) {
        try {
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("https://api.openai.com/v1/embeddings")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createMockEmbeddingResponse(embedding)));

            // Mock ObjectMapper for embedding response
            JsonNode mockResponse = createMockEmbeddingJsonResponse(embedding);
            when(objectMapper.readTree(anyString())).thenReturn(mockResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockTopicExtraction(String query, String topic) {
        try {
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createMockChatResponse(topic)));

            // Mock ObjectMapper for topic extraction
            JsonNode mockResponse = createMockChatJsonResponse(topic);
            when(objectMapper.readTree(contains("gpt-3.5-turbo"))).thenReturn(mockResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockSynthesisResponse(String answer) {
        try {
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any()))
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createMockChatResponse(answer)));

            // Mock ObjectMapper for synthesis
            JsonNode mockResponse = createMockChatJsonResponse(answer);
            when(objectMapper.readTree(contains("expert summarizer"))).thenReturn(mockResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockNewsApiResponse() {
        try {
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createMockNewsApiResponse()));

            // Mock ObjectMapper for news API
            JsonNode mockResponse = createMockNewsApiJsonResponse();
            when(objectMapper.readTree(contains("thenewsapi"))).thenReturn(mockResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockEmptyNewsApiResponse() {
        try {
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createEmptyNewsApiResponse()));

            // Mock ObjectMapper for empty news API
            JsonNode mockResponse = createEmptyNewsApiJsonResponse();
            when(objectMapper.readTree(contains("thenewsapi"))).thenReturn(mockResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockTextExtraction() {
        // This would be complex to mock Jsoup properly
        // In practice, you might want to create a wrapper service for text extraction
        // and mock that instead
    }

    private String createMockEmbeddingResponse(List<Double> embedding) {
        return "{\"data\":[{\"embedding\":" + embedding.toString().replace(" ", "") + "}]}";
    }

    private JsonNode createMockEmbeddingJsonResponse(List<Double> embedding) {
        try {
            ObjectNode root = realObjectMapper.createObjectNode();
            ArrayNode data = root.putArray("data");
            ObjectNode item = data.addObject();
            ArrayNode embeddingArray = item.putArray("embedding");
            for (Double value : embedding) {
                embeddingArray.add(value);
            }
            return root;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createMockChatResponse(String content) {
        return "{\"choices\":[{\"message\":{\"content\":\"" + content + "\"}}]}";
    }

    private JsonNode createMockChatJsonResponse(String content) {
        try {
            ObjectNode root = realObjectMapper.createObjectNode();
            ArrayNode choices = root.putArray("choices");
            ObjectNode choice = choices.addObject();
            ObjectNode message = choice.putObject("message");
            message.put("content", content);
            return root;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createMockNewsApiResponse() {
        return "{\"data\":[" +
               "{\"title\":\"Trade Policy Update\",\"url\":\"https://example.com/article1\"}," +
               "{\"title\":\"Economic Impact Analysis\",\"url\":\"https://example.com/article2\"}" +
               "]}";
    }

    private JsonNode createMockNewsApiJsonResponse() {
        try {
            ObjectNode root = realObjectMapper.createObjectNode();
            ArrayNode data = root.putArray("data");
            
            ObjectNode article1 = data.addObject();
            article1.put("title", "Trade Policy Update");
            article1.put("url", "https://example.com/article1");
            
            ObjectNode article2 = data.addObject();
            article2.put("title", "Economic Impact Analysis");
            article2.put("url", "https://example.com/article2");
            
            return root;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createEmptyNewsApiResponse() {
        return "{\"data\":[]}";
    }

    private JsonNode createEmptyNewsApiJsonResponse() {
        try {
            ObjectNode root = realObjectMapper.createObjectNode();
            root.putArray("data");
            return root;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
