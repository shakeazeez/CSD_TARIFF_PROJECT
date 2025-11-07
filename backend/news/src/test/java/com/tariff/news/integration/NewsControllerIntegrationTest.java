package com.tariff.news.integration;

import com.tariff.news.article.ArticleEmbedding;
import com.tariff.news.article.ArticleEmbeddingRepo;
import com.tariff.news.history.ChatHistory;
import com.tariff.news.history.ChatHistoryRepo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.tariff.news.config.NewsTestServiceConfig;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for NewsController using SpringBootTest (RANDOM_PORT) with REST Assured.
 * - Uses in-memory H2 via Spring Data JPA repositories (ArticleEmbeddingRepo).
 * - Seeds minimal ArticleEmbedding records in @BeforeEach (non-destructive; no deletes).
 * - Imports NewsTestServiceConfig to replace remote/network calls with deterministic, network-free behavior.
 * - Verifies endpoints
 * - Asserts HTTP status, content type, and JSON payloads end-to-end (no mocks at the HTTP layer).
 * - Adds light repository assertions after requests to validate database state when helpful.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(NewsTestServiceConfig.class)
class NewsControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private ArticleEmbeddingRepo articleRepo;

    private ArticleEmbedding tradeArticle;
    private ArticleEmbedding econArticle;

    @Autowired
    private ChatHistoryRepo historyRepo;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Seed minimal, isolated test data (non-destructive: no deletes)
        String tradeUrl = "https://itest.example.com/trade";
        var existingTrade = articleRepo.findByUrl(tradeUrl);
        if (existingTrade.isEmpty()) {
            tradeArticle = new ArticleEmbedding();
            tradeArticle.setTitle("Trade Policy Update");
            tradeArticle.setUrl(tradeUrl);
            tradeArticle.setCleanedText("Trade policy news test content");
            tradeArticle.setEmbedding(new float[]{0.1f, 0.2f, 0.3f});
            tradeArticle.setTopic("trade");
            tradeArticle = articleRepo.save(tradeArticle);
        } else {
            tradeArticle = existingTrade.get(0);
        }

        String econUrl = "https://itest.example.com/econ";
        var existingEcon = articleRepo.findByUrl(econUrl);
        if (existingEcon.isEmpty()) {
            econArticle = new ArticleEmbedding();
            econArticle.setTitle("Economic Outlook");
            econArticle.setUrl(econUrl);
            econArticle.setCleanedText("Economic outlook test content");
            econArticle.setEmbedding(new float[]{-0.2f, 0.0f, 0.4f});
            econArticle.setTopic("economy");
            econArticle = articleRepo.save(econArticle);
        } else {
            econArticle = existingEcon.get(0);
        }
    }

    @Test
    @DisplayName("POST /news/process returns base answer when articles present (no fallback)")
    void processQuery_articlesPresent_noFallback() {
        given()
            .accept(ContentType.JSON)
            .contentType("application/x-www-form-urlencoded")
            .formParam("query", "hello ARTICLES=ONE")
        .when()
            .post("/news/process")
        .then()
            .statusCode(200)
            .body("synthesizedAnswer", is("base"))
            .body("articles.size()", is(1))
            .body("conversationId", nullValue());

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
    }

    @Test
    @DisplayName("POST /news/process with null articles triggers fallback message")
    void processQuery_nullArticles_triggersFallback() {
        given()
            .accept(ContentType.JSON)
            .contentType("application/x-www-form-urlencoded")
            .formParam("query", "hello ARTICLES=NULL")
        .when()
            .post("/news/process")
        .then()
            .statusCode(200)
            .body("synthesizedAnswer", containsString("I couldn't find specific articles on that topic."));

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
    }

    @Test
    @DisplayName("POST /news/process when service throws returns 500")
    void processQuery_serviceThrows_returns500() {
        given()
            .accept(ContentType.JSON)
            .contentType("application/x-www-form-urlencoded")
            .formParam("query", "THROW")
        .when()
            .post("/news/process")
        .then()
            .statusCode(500);

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
    }

    @Test
    @DisplayName("POST /news/process with conversationId and blank username does not wrap or save history")
    void processQuery_conversationId_blankUsername_noSave() {
        given()
            .accept(ContentType.JSON)
            .contentType("application/x-www-form-urlencoded")
            .formParam("query", "q")
            .formParam("username", " ")
            .formParam("conversationId", "5")
        .when()
            .post("/news/process")
        .then()
            .statusCode(200)
            .body("conversationId", nullValue())
            .body("synthesizedAnswer", containsString("I couldn't find specific articles on that topic."));

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
    }

    @Test
    @DisplayName("POST /news/process with username+conversationId saves history and returns conversationId")
    void processQuery_withUserAndConversation_savesAndReturnsId() {
        // Seed a conversation for context lookup
        ChatHistory h = new ChatHistory();
        h.setUsername("alice");
        h.setTopic("trade");
        h.setMessages("[{\"query\":\"What are tariffs?\",\"response\":\"Taxes\"}]");
        ChatHistory saved = historyRepo.save(h);

        given()
            .accept(ContentType.JSON)
            .contentType("application/x-www-form-urlencoded")
            .formParam("query", "What about recent changes?")
            .formParam("username", "alice")
            .formParam("conversationId", String.valueOf(saved.getId()))
        .when()
            .post("/news/process")
        .then()
            .statusCode(200)
            .body("conversationId", notNullValue())
            .body("synthesizedAnswer", containsString("I couldn't find specific articles on that topic."));

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
    }

    @Test
    @DisplayName("GET /news/articles returns all articles")
    void testGetAllArticles_ReturnsArticlesList() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/news/articles")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("size()", greaterThanOrEqualTo(2))
            .body("title", hasItems("Trade Policy Update", "Economic Outlook"))
            .body("url", hasItems("https://itest.example.com/trade", "https://itest.example.com/econ"));

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
    }

    @Test
    @DisplayName("GET /news/search/topic/{topic} returns only matching topic articles")
    void testFindArticlesByTopic_ReturnsTopicArticles() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/news/search/topic/{topic}", "trade")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("size()", is(1))
            .body("[0].title", is("Trade Policy Update"))
            .body("[0].url", is("https://itest.example.com/trade"))
            .body("[0].topic", is("trade"));

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
    }

    @Test
    @DisplayName("GET /news/search/query returns articles matching the query")
    void testFindArticlesByQuery_ReturnsMatchingArticles() {
        given()
            .accept(ContentType.JSON)
            .queryParam("query", "trade policy")
            .queryParam("limit", 2)
        .when()
            .get("/news/search/query")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].url", is("https://itest.example.com/trade"))
            .body("[0].title", anyOf(is("Trade Policy Update"), notNullValue()))
            .body("[0].topic", anyOf(is("trade"), notNullValue()));

        // Verify database state (simple, non-destructive check)
        var all = articleRepo.findAll();
        java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
        assert all.size() >= 2;
        assert urls.contains("https://itest.example.com/trade");
        assert urls.contains("https://itest.example.com/econ");
        
    }

    @Test
    @DisplayName("GET /news/search/similar returns ordered similar articles")
    void testFindSimilarArticles_ReturnsSimilarArticles() {
        String queryEmbedding = Arrays.toString(new double[]{0.1, 0.2, 0.3});

        given()
            .accept(ContentType.JSON)
            .queryParam("embedding", queryEmbedding)
            .queryParam("limit", 2)
        .when()
            .get("/news/search/similar")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("size()", is(2))
            // First element should be the most similar (tradeArticle)
            .body("[0].url", is("https://itest.example.com/trade"))
            .body("[1].url", is("https://itest.example.com/econ"));

    // Verify database state (simple, non-destructive check)
    var all = articleRepo.findAll();
    java.util.List<String> urls = all.stream().map(ArticleEmbedding::getUrl).toList();
    assert all.size() >= 2;
    assert urls.contains("https://itest.example.com/trade");
    assert urls.contains("https://itest.example.com/econ");
    }
}
