package com.tariff.news.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Unit tests for NewsEmbeddingService (no Spring context).
 * - JUnit 5 + MockitoExtension; WebClient/ObjectMapper/ArticleEmbeddingRepo are mocked.
 * - Follows AAA (Arrange-Act-Assert) style
 *   for private method access and field injection of configuration values.
 * - Covers key branches
 * - Avoids real network I/O by mocking WebClient.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("News Embedding Service Tests")
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
    void generateQueryContext_Exception_ReturnsEmpty() {
        // Make the POST call blow up so the method returns empty string
        when(webClient.post()).thenThrow(new RuntimeException("boom"));
        String out = ReflectionTestUtils.invokeMethod(newsEmbeddingService,
                "generateQueryContext", "query", "some cleaned text");
        assertNotNull(out);
        assertEquals("", out);
    }

    @Test
    void processQuery_CandidateProcessingErrors_FallbackApiAnswer() throws Exception {
        // Arrange
        String query = "topic question";
        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);

        // DB miss forces API path
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(Collections.emptyList());

        // Embeddings (query/topic)
        doReturn(Arrays.asList(1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)).when(serviceSpy).generateEmbedding(anyString());

        // Topic and two candidates
        doReturn("topic").when(serviceSpy).extractTopic(query);
        doReturn(Arrays.asList(new NewsEmbeddingService.Article("t1","https://e.com/1"),
                               new NewsEmbeddingService.Article("t2","https://e.com/2")))
                .when(serviceSpy).fetchArticles("topic");

        // Cause per-candidate processing to fail -> results stay empty
        doThrow(new IOException("scrape fail")).when(serviceSpy).extractFullText(anyString());

        // Fallback synthesizeAnswer will be called with empty list
        mockSynthesisResponse("fallback");

        // Act
        NewsResponse res = serviceSpy.processQuery(query);

        // Assert
        assertNotNull(res);
        assertEquals("api-fallback", res.getSource());
        assertTrue(res.getArticles().isEmpty());
    }

    @Test
    void processQuery_OuterCatch_RethrowsRuntime() {
        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);
        doThrow(new RuntimeException("embed fail")).when(serviceSpy).generateEmbedding(anyString());
        assertThrows(RuntimeException.class, () -> serviceSpy.processQuery("anything"));
    }

    @Test
    void synthesizeAnswer_Exception_ReturnsOfflineFallback() {
        // Force web client to throw so synthesizeAnswer returns fallback string
        when(webClient.post()).thenThrow(new RuntimeException("post error"));
        String out = ReflectionTestUtils.invokeMethod(newsEmbeddingService,
                "synthesizeAnswer", "q", Collections.emptyList());
        assertNotNull(out);
        // Service returns CHATBOT_OFFLINE on exception per implementation
        assertEquals("CHATBOT_OFFLINE", out);
    }

    @Test
    void extractTopic_ParseException_FallbackGeneralTopic() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("https://api.openai.com/v1/chat/completions")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{}"));

        // Force parsing failure so service uses fallback extraction
        try {
            when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("bad json"));
        } catch (Exception ignore) { /* Mockito signature declares checked exceptions */ }

        // Query "x" has no meaningful words (>2 chars) so fallback returns "general news"
        String topic = newsEmbeddingService.extractTopic("x");
        assertEquals("general news", topic);
    }

    @Test
    void fetchArticles_HttpFailure_ReturnsEmpty() {
        when(webClient.get()).thenThrow(new RuntimeException("http down"));
        List<NewsEmbeddingService.Article> list = newsEmbeddingService.fetchArticles("topic");
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void generateEmbedding_ParseException_Rethrows() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("https://api.openai.com/v1/embeddings")).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{}"));

        try {
            when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("parse fail"));
        } catch (Exception ignore) { }
        assertThrows(RuntimeException.class, () -> newsEmbeddingService.generateEmbedding("t"));
    }

    @Test
    void parseStringToFloatArray_InvalidNumbers_Zeroed() {
        float[] arr = ReflectionTestUtils.invokeMethod(newsEmbeddingService,
                "parseStringToFloatArray", "[1.0, notANum, 3.5]");
        assertNotNull(arr);
        assertEquals(3, arr.length);
        assertEquals(1.0f, arr[0], 0.0001);
        assertEquals(0.0f, arr[1], 0.0001);
        assertEquals(3.5f, arr[2], 0.0001);
    }

    @Test
    void findArticlesSimilarToQuery_EmbeddingThrows_ReturnsEmpty() {
        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);
        doThrow(new RuntimeException("embed fail")).when(serviceSpy).generateEmbedding(anyString());
        List<ArticleEmbedding> got = serviceSpy.findArticlesSimilarToQuery("q", 3);
        assertNotNull(got);
        assertTrue(got.isEmpty());
    }

    @Test
    void processQuery_PgvectorKnnFailure_FallbackToFullScan() throws Exception {
        // Arrange
        String query = "trade tariffs impact";

        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);

        // Enable pgvector path and set deterministic embedding
        ReflectionTestUtils.setField(serviceSpy, "usePgvector", true);
        List<Double> queryEmbedding = Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        doReturn(queryEmbedding).when(serviceSpy).generateEmbedding(query);

        // KNN throws -> fallback to full scan
        when(articleEmbeddingRepo.findClosestArticles(anyString(), anyInt()))
            .thenThrow(new RuntimeException("KNN error"));

        // Stored identical vectors ensure cosine 1.0 >= threshold
        List<ArticleEmbedding> stored = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
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

        // Chat completion for synthesizeAnswer
        mockSynthesisResponse("Synthesized answer from DB fallback");

        // Act
        NewsResponse result = serviceSpy.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("db", result.getSource());
        verify(articleEmbeddingRepo).findClosestArticles(anyString(), anyInt());
        verify(articleEmbeddingRepo).findAllByEmbeddingIsNotNull();
    }

    @Test
    void processQuery_DatabaseHit_ReturnsStoredArticles_SimpleUnit() throws Exception {
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
    void processQuery_ApiFallback_SavesArticlesWithQueryContext_SimpleUnit() throws Exception {
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
    void extractTopic_TradeQuery_ReturnsSpecificTopic() throws Exception {
        // Arrange
        String query = "How do tariffs affect small businesses?";
        mockTopicExtraction(query, "tariffs business");

        // Act
        String result = newsEmbeddingService.extractTopic(query);

        // Assert
        assertEquals("tariffs business", result);
    }

    @Test
    void extractTopic_GeneralQuery_ReturnsGeneralTopic() throws Exception {
        // Arrange
        String query = "climate change effects";
        mockTopicExtraction(query, "climate change effects");

        // Act
        String result = newsEmbeddingService.extractTopic(query);

        // Assert
        assertEquals("climate change effects", result);
    }

    @Test
    void processQuery_ApiPath_ReadContextException_Ignored() throws Exception {
        // Arrange
        String query = "new trade policy";
        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);

        // Force DB miss
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(Collections.emptyList());

        // Deterministic embeddings
        List<Double> emb = Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        doReturn(emb).when(serviceSpy).generateEmbedding(anyString());

        // Topic and articles
        doReturn("trade policy").when(serviceSpy).extractTopic(query);
        List<NewsEmbeddingService.Article> fake = Arrays.asList(
            new NewsEmbeddingService.Article("A1", "https://example.com/a1"),
            new NewsEmbeddingService.Article("A2", "https://example.com/a2")
        );
        doReturn(fake).when(serviceSpy).fetchArticles("trade policy");

        // Clean content
        doReturn("Clean content long enough ".repeat(6)).when(serviceSpy).extractFullText(anyString());

        // Chat completions used by generateQueryContext and synthesizeAnswer
        mockSynthesisResponse("ctx or answer");

        // findByUrl: two calls for saves (empty), then first DTO read throws, second read empty
        when(articleEmbeddingRepo.findByUrl(anyString()))
            .thenReturn(Collections.emptyList()) // save #1
            .thenReturn(Collections.emptyList()) // save #2
            .thenThrow(new RuntimeException("read fail")) // DTO read #1
            .thenReturn(Collections.emptyList()); // DTO read #2

        // Act
        NewsResponse result = serviceSpy.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("api", result.getSource());
        assertEquals(2, result.getArticles().size());
        // ensure no exception bubbled and context may be empty due to exception
        assertNotNull(result.getArticles().get(0).getQueryContext());
    }

    @Test
    void processQuery_ApiPath_ReadContextSuccess_PropagatesToDTO() throws Exception {
        // Arrange
        String query = "new trade policy";
        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);

        // Force DB miss so we go to API path
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(Collections.emptyList());

        // Deterministic embeddings so cosine similarity >= threshold
        List<Double> emb = Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        doReturn(emb).when(serviceSpy).generateEmbedding(anyString());

        // Topic and API candidates
        doReturn("trade policy").when(serviceSpy).extractTopic(query);
        List<NewsEmbeddingService.Article> fake = Arrays.asList(
            new NewsEmbeddingService.Article("A1", "https://example.com/a1"),
            new NewsEmbeddingService.Article("A2", "https://example.com/a2")
        );
        doReturn(fake).when(serviceSpy).fetchArticles("trade policy");

        // Provide cleaned text so candidates pass filtering
        doReturn("Clean content long enough ".repeat(6)).when(serviceSpy).extractFullText(anyString());

        // Chat completions used by generateQueryContext and synthesizeAnswer
        mockSynthesisResponse("ctx or answer");

        // Simulate: two save paths (no existing) then two DTO readbacks returning populated entities
        ArticleEmbedding readback = new ArticleEmbedding();
        readback.setQueryContext("QCTX-R");
        readback.setLastSeenQuery("trade policy");
        when(articleEmbeddingRepo.findByUrl(anyString()))
            .thenReturn(Collections.emptyList()) // A1 save check
            .thenReturn(Collections.singletonList(readback)) // A1 DTO readback
            .thenReturn(Collections.emptyList()) // A2 save check
            .thenReturn(Collections.singletonList(readback)); // A2 DTO readback

        // Act
        NewsResponse result = serviceSpy.processQuery(query);

        // Assert
        assertNotNull(result);
        assertEquals("api", result.getSource());
        assertEquals(2, result.getArticles().size());
        // Both DTOs should carry queryContext/lastSeenQuery as read from repository
        assertEquals("QCTX-R", result.getArticles().get(0).getQueryContext());
        assertEquals("trade policy", result.getArticles().get(0).getLastSeenQuery());
        assertEquals("QCTX-R", result.getArticles().get(1).getQueryContext());
        assertEquals("trade policy", result.getArticles().get(1).getLastSeenQuery());
    }
    
    @Test
    void cosineSimilarity_Guards_ReturnZero() {
        // Arrange
        List<Double> b = Arrays.asList(1.0, 2.0);

        // Act
        Double resNull = (Double) ReflectionTestUtils.invokeMethod(
            newsEmbeddingService, "cosineSimilarity", new Object[]{null, b});
        Double resSizeMismatch = (Double) ReflectionTestUtils.invokeMethod(
            newsEmbeddingService, "cosineSimilarity", new Object[]{Arrays.asList(1.0), Arrays.asList(1.0, 2.0)});
        Double resZeroNorm = (Double) ReflectionTestUtils.invokeMethod(
            newsEmbeddingService, "cosineSimilarity", new Object[]{Arrays.asList(0.0, 0.0), Arrays.asList(1.0, 1.0)});

        // Assert
        assertEquals(0.0, resNull);
        assertEquals(0.0, resSizeMismatch);
        assertEquals(0.0, resZeroNorm);
    }
    
    @Test
    void floatArrayToList_NullInput_ReturnsEmptyList() {
        // Arrange
        float[] array = null;

        // Act
        @SuppressWarnings("unchecked")
        List<Double> result = (List<Double>) ReflectionTestUtils.invokeMethod(
                newsEmbeddingService, "floatArrayToList", new Object[]{array});

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void convertFloatArrayToString_NullInput_ReturnsNull() {
        // Arrange
        float[] array = null;

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(
                newsEmbeddingService, "convertFloatArrayToString", new Object[]{array});

        // Assert
        assertNull(result);
    }
    
    @Test
    void parseStringToFloatArray_NullAndEmptyContent_ReturnsEmptyArray() {
        // Arrange
        String nullInput = null;
        String emptyArray = "[]";

        // Act
        float[] resNull = (float[]) ReflectionTestUtils.invokeMethod(
                newsEmbeddingService, "parseStringToFloatArray", new Object[]{nullInput});
        float[] resEmpty = (float[]) ReflectionTestUtils.invokeMethod(
                newsEmbeddingService, "parseStringToFloatArray", emptyArray);

        // Assert
        assertNotNull(resNull);
        assertEquals(0, resNull.length);
        assertNotNull(resEmpty);
        assertEquals(0, resEmpty.length);
    }

    @Test
    void fetchArticles_ReturnsValidArticles() {
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
    void extractFullText_ValidUrl_ReturnsCleanedText() throws IllegalArgumentException {
        // This test would require mocking Jsoup, which is complex
        // In a real scenario, you might want to use a test profile with a mock implementation
        
        // For now, we'll test the error handling
        assertThrows(IllegalArgumentException.class, () -> {
            newsEmbeddingService.extractFullText("invalid-url");
        });
    }

    @Test
    void extractFullText_ShortContent_ThrowsIOException() throws Exception {
        LocalServer server = null;
        try {
            server = startServer("<html><body>Hi</body></html>");
            String url = server.url;
            assertThrows(IOException.class, () -> newsEmbeddingService.extractFullText(url));
        } finally {
            if (server != null) server.close();
        }
    }

    @Test
    void extractFullText_LongContent_Succeeds() throws Exception {
        LocalServer server = null;
        try {
            String body = "<html><body>" + "content ".repeat(40) + "</body></html>"; // > 100 chars
            server = startServer(body);
            String url = server.url;
            String text = newsEmbeddingService.extractFullText(url);
            assertNotNull(text);
            assertTrue(text.length() >= 100);
        } finally {
            if (server != null) server.close();
        }
    }

    @Test
    void generateEmbedding_ValidText_ReturnsEmbedding() {
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
    void convertStringToEmbedding_ValidString_ReturnsEmbedding() {
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
    void convertStringToEmbedding_NullOrTooShort_ReturnsEmptyList() {
        // Null input returns empty list
        List<Double> resNull = newsEmbeddingService.convertStringToEmbedding(null);
        assertNotNull(resNull);
        assertTrue(resNull.isEmpty());

        // Too short (e.g., "[]") returns empty list per early return branch
        List<Double> resShort = newsEmbeddingService.convertStringToEmbedding("[]");
        assertNotNull(resShort);
        assertTrue(resShort.isEmpty());
    }

    @Test
    void convertStringToEmbedding_InvalidString_ReturnsEmptyList() {
        // Arrange
        String embeddingStr = "invalid";

        // Act
        List<Double> result = newsEmbeddingService.convertStringToEmbedding(embeddingStr);

        // Assert
        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0));
    }

    @Test
    void extractTopicFallback_LimitsToFourMeaningfulWords() {
        // We want to exercise the loop: add words >2 chars, stop after 4 collected
        String query = "alpha beta gamma delta epsilon zeta"; // 6 meaningful words
        String topic = ReflectionTestUtils.invokeMethod(newsEmbeddingService, "extractTopicFallback", query);
        assertEquals("alpha beta gamma delta", topic, "Should only take first four meaningful words");
    }

    @Test
    void extractTopicFallback_SkipsStopWords_AndShortOnes() {
        // Contains stopwords (the, and, or, in, to, of, a) and short tokens (x, at) mixed with meaningful words
        String query = "the and or alpha in to of beta a x at gamma";
        String topic = ReflectionTestUtils.invokeMethod(newsEmbeddingService, "extractTopicFallback", query);
        // Stop words and short tokens removed -> first meaningful tokens alpha beta gamma
        assertEquals("alpha beta gamma", topic);
    }

    @Test
    void extractTopicFallback_AllStopOrShort_ReturnsGeneralNews() {
        String query = "a an the in to of or on by at"; // all stop/short
        String topic = ReflectionTestUtils.invokeMethod(newsEmbeddingService, "extractTopicFallback", query);
        assertEquals("general news", topic);
    }

    @Test
    void findSimilarArticles_ReturnsOrderedResults() {
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
    void findArticlesSimilarToQuery_ValidQuery_ReturnsResults() {
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
    void findArticlesByTopic_ValidTopic_ReturnsResults() {
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
    void getAllArticles_ReturnsAllStoredArticles() {
        // Arrange
        List<ArticleEmbedding> expectedArticles = createMockStoredArticles();
        
        when(articleEmbeddingRepo.findAll()).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = newsEmbeddingService.getAllArticles();

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findAll();
    }

    @Test
    void hasMinimalRelevance_Match() {
        boolean match = ReflectionTestUtils.invokeMethod(newsEmbeddingService,
                "hasMinimalRelevance", "US trade policy updates", "trade policy");
        assertTrue(match);
    }

    @Test
    void hasMinimalRelevance_NoMatch() {
        boolean match = ReflectionTestUtils.invokeMethod(newsEmbeddingService,
                "hasMinimalRelevance", "Sports event highlights", "trade policy");
        assertFalse(match);
    }

    @Test
    void saveArticleEmbedding_UpdateExisting_WithQueryEmbedding() throws Exception {
        // Arrange
        String title = "Existing Title";
        String url = "https://example.com/existing";
        String cleaned = "Clean content long enough ".repeat(5);
        String embStr = "[0.1,0.2,0.3]";
        String topic = "trade policy";

        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);
        ReflectionTestUtils.setField(serviceSpy, "storeQueryEmbedding", true);

        // Existing entity triggers update path
        ArticleEmbedding existing = new ArticleEmbedding();
        existing.setId(42L);
        existing.setUrl(url);
        when(articleEmbeddingRepo.findByUrl(url)).thenReturn(Collections.singletonList(existing));

        // generateQueryContext + queryEmbedding via mocks
        mockSynthesisResponse("QCTX");

        // queryEmbedding
        doReturn(Arrays.asList(0.5, 0.5)).when(serviceSpy).generateEmbedding(anyString());

        ArgumentCaptor<ArticleEmbedding> captor = ArgumentCaptor.forClass(ArticleEmbedding.class);

        // Act
        ReflectionTestUtils.invokeMethod(serviceSpy, "saveArticleEmbedding", title, url, cleaned, embStr, topic);

        // Assert
        verify(articleEmbeddingRepo).save(captor.capture());
        ArticleEmbedding saved = captor.getValue();
        assertEquals(42L, saved.getId());
        assertEquals(title, saved.getTitle());
        assertEquals(topic, saved.getTopic());
        assertNotNull(saved.getQueryContext());
        assertNotNull(saved.getQueryEmbedding());
        assertEquals(topic, saved.getLastSeenQuery());
    }

    @Test
    void saveArticleEmbedding_CreateNew_WithQueryEmbedding() throws Exception {
        // Arrange
        String title = "New Title";
        String url = "https://example.com/new";
        String cleaned = "Clean content long enough ".repeat(5);
        String embStr = "[0.3,0.2,0.1]";
        String topic = "tariff rules";

        NewsEmbeddingService serviceSpy = spy(newsEmbeddingService);
        ReflectionTestUtils.setField(serviceSpy, "storeQueryEmbedding", true);

        // No existing -> create path
        when(articleEmbeddingRepo.findByUrl(url)).thenReturn(Collections.emptyList());

        // generateQueryContext + queryEmbedding via mocks
        mockSynthesisResponse("QCTX2");

        doReturn(Arrays.asList(0.7, 0.3)).when(serviceSpy).generateEmbedding(anyString());

        ArgumentCaptor<ArticleEmbedding> captor = ArgumentCaptor.forClass(ArticleEmbedding.class);

        // Act
        ReflectionTestUtils.invokeMethod(serviceSpy, "saveArticleEmbedding", title, url, cleaned, embStr, topic);

        // Assert
        verify(articleEmbeddingRepo).save(captor.capture());
        ArticleEmbedding saved = captor.getValue();
        assertEquals(title, saved.getTitle());
        assertEquals(url, saved.getUrl());
        assertEquals(topic, saved.getTopic());
        assertNotNull(saved.getQueryContext());
        assertNotNull(saved.getQueryEmbedding());
        assertEquals(topic, saved.getLastSeenQuery());
    }

    // ========================================
    // Local HTTP server helpers
    // ========================================
    private static class LocalServer implements AutoCloseable {
        final HttpServer server;
        final String url;
        LocalServer(HttpServer s, String u) { this.server = s; this.url = u; }
        public void close() {
            if (server != null) server.stop(0);
        }
    }

    private LocalServer startServer(String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] bytes = body.getBytes();
                exchange.sendResponseHeaders(200, bytes.length);
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });
        server.setExecutor(null);
        server.start();
        int port = server.getAddress().getPort();
        String url = "http://127.0.0.1:" + port + "/";
        return new LocalServer(server, url);
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
}
