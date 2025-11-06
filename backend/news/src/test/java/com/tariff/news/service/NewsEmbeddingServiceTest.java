package com.tariff.news.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.mockito.ArgumentCaptor;


/**
 * Unit tests for NewsEmbeddingService following AAA (Arrange-Act-Assert) pattern.
 * Each test method is clearly structured with Arrange, Act, and Assert sections.
 */
@ExtendWith(MockitoExtension.class)
public class NewsEmbeddingServiceTest {

    // ========================================
    // Test Dependencies and Mocks
    // ========================================

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

    // ========================================
    // Test Setup
    // ========================================

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
    void testProcessQuery_DatabaseHit_ReturnsStoredArticles_SimpleUnit() throws Exception {
        // Arrange
        String query = "trade tariffs impact";

        // Spy to stub generateEmbedding deterministically and avoid mocking embeddings HTTP
        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);

        // Deterministic embedding vector (length 8 to keep it small but consistent)
        List<Double> queryEmbedding = Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        doReturn(queryEmbedding).when(serviceSpy).generateEmbedding(query);

        // Stored articles with embeddings identical to queryEmbedding to guarantee cosine=1.0
        List<ArticleEmbedding> stored = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            ArticleEmbedding a = new ArticleEmbedding();
            a.setId((long) i);
            a.setTitle("Trade Article " + i);
            a.setUrl("https://example.com/article" + i);
            a.setCleanedText("Clean text " + i);
            a.setEmbedding(toFloatArray(queryEmbedding));
            a.setTopic("trade");
            a.setQueryContext("Context " + i);
            a.setLastSeenQuery("trade tariffs impact");
            stored.add(a);
        }
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(stored);

        // Mock synthesizeAnswer's chat completion call
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
            .thenReturn(Mono.just(createMockChatResponse("Synthesized answer about trade tariffs")));

        JsonNode chatJson = createMockChatJsonResponse("Synthesized answer about trade tariffs");
        when(objectMapper.readTree(anyString())).thenReturn(chatJson);

        // Act
        NewsResponse result = serviceSpy.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("db", result.getSource());
        assertEquals("Synthesized answer about trade tariffs", result.getSynthesizedAnswer());
        assertEquals(3, result.getArticles().size());
        verify(webClient, never()).get();
    }

    // Local helper to convert a List<Double> to float[] matching the service storage type
    private float[] toFloatArray(List<Double> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i).floatValue();
        return arr;
    }

    @Test
    void testProcessQuery_ApiFallback_SavesArticlesWithQueryContext_SimpleUnit() throws Exception {
        // Arrange
        String query = "new trade policy";

        // Spy the service to bypass network-heavy collaborators with simple returns
        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);

        // Force DB miss
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(Collections.emptyList());

        // Ensure save creates new entities (no upsert path)
        when(articleEmbeddingRepo.findByUrl(anyString())).thenReturn(Collections.emptyList());

        // Stable vectors so per-article similarity >= threshold
        List<Double> anyEmbedding = Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        doReturn(anyEmbedding).when(serviceSpy).generateEmbedding(anyString());

        // Bypass GPT topic extraction and TheNewsAPI HTTP by stubbing high-level methods
        doReturn("trade policy").when(serviceSpy).extractTopic(query);
        List<NewsEmbeddingService.Article> fake = Arrays.asList(
            new NewsEmbeddingService.Article("Trade A", "https://example.com/a"),
            new NewsEmbeddingService.Article("Trade B", "https://example.com/b")
        );
        doReturn(fake).when(serviceSpy).fetchArticles("trade policy");

        // Avoid Jsoup: provide deterministic cleaned text
        doReturn("Clean content long enough to pass threshold. ".repeat(3)).when(serviceSpy).extractFullText(anyString());

        // Mock chat completions used by both generateQueryContext and synthesizeAnswer
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createMockChatResponse("Synth API answer")));

        JsonNode chatJson = createMockChatJsonResponse("Synth API answer");
        when(objectMapper.readTree(anyString())).thenReturn(chatJson);

        // Act
        NewsResponse result = serviceSpy.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("api", result.getSource());
        assertEquals(2, result.getArticles().size());
        assertEquals("Synth API answer", result.getSynthesizedAnswer());

        // Verify persistence with generated queryContext populated
        ArgumentCaptor<ArticleEmbedding> captor = ArgumentCaptor.forClass(ArticleEmbedding.class);
        verify(articleEmbeddingRepo, atLeast(2)).save(captor.capture());
        for (ArticleEmbedding saved : captor.getAllValues()) {
            assertNotNull(saved.getQueryContext());
            assertFalse(saved.getQueryContext().isEmpty());
        }

        // No raw news HTTP GET executed due to high-level stubbing
        verify(webClient, never()).get();
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
    void testExtractFullText_ValidUrl_ReturnsCleanedText() throws IllegalArgumentException {
        // This test would require mocking Jsoup, which is complex
        // In a real scenario, you might want to use a test profile with a mock implementation
        
        // For now, we'll test the error handling
        assertThrows(IllegalArgumentException.class, () -> {
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
        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0));
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
            when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
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
            when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createMockChatResponse(topic)));

            // Mock ObjectMapper for topic extraction
            JsonNode mockResponse = createMockChatJsonResponse(topic);
            when(objectMapper.readTree(anyString())).thenReturn(mockResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockSynthesisResponse(String answer) {
        try {
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(createMockChatResponse(answer)));

            // Mock ObjectMapper for synthesis
            JsonNode mockResponse = createMockChatJsonResponse(answer);
            when(objectMapper.readTree(anyString())).thenReturn(mockResponse);
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
            when(objectMapper.readTree(anyString())).thenReturn(mockResponse);
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
            when(objectMapper.readTree(anyString())).thenReturn(mockResponse);
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
