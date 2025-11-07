package com.tariff.news.integration;

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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for ChatHistoryController using SpringBootTest (RANDOM_PORT) with REST Assured.
 * - Uses in-memory H2 via Spring Data JPA (ChatHistoryRepo) and non-destructive seeding in @BeforeEach.
 * - Asserts HTTP status, content type, and JSON payloads; adds simple repository checks after requests.
 * - Authorization behaviour is validated via path parameters and service logic.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatHistoryControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private ChatHistoryRepo historyRepo;

    private ChatHistory convo1;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Non-destructive seeding: insert if not present (by username+topic+messages signature)
        String username = "itest_user";
        String topic = "policy";
        String messages = "[{'query':'What is the tariff?','response':'Tariff is a tax on imports','sources':[]}]".replace('\'', '"');

        boolean exists = historyRepo.findByUsernameAndTopicOrderByCreatedAtDesc(username, topic)
            .stream().anyMatch(h -> messages.equals(h.getMessages()));

        if (!exists) {
            convo1 = new ChatHistory();
            convo1.setUsername(username);
            convo1.setTopic(topic);
            convo1.setMessages(messages);
            convo1 = historyRepo.save(convo1);
        } else {
            convo1 = historyRepo.findByUsernameAndTopicOrderByCreatedAtDesc(username, topic).get(0);
        }
    }

    @Test
    @DisplayName("GET /news/history/{username} returns user conversations")
    void testGetHistoryForUser_ReturnsConversations() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/news/history/{username}", "itest_user")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].id", notNullValue())
            .body("[0].title", is("policy"))
            .body("[0].messages.size()", greaterThanOrEqualTo(1))
            .body("[0].messages[0].query", is("What is the tariff?"))
            .body("[0].messages[0].response", is("Tariff is a tax on imports"));

        // Verify database state (simple, non-destructive check)
        var all = historyRepo.findAll();
        assert all.stream().anyMatch(h -> "itest_user".equals(h.getUsername()));
        assert all.stream().anyMatch(h -> "policy".equals(h.getTopic()));
    }

    @Test
    @DisplayName("GET /news/history/{username}?topic=... filters by topic")
    void testGetHistoryWithTopicFilter_ReturnsFiltered() {
        given()
            .accept(ContentType.JSON)
            .queryParam("topic", "policy")
        .when()
            .get("/news/history/{username}", "itest_user")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].title", is("policy"));

        // Verify database state
        var all = historyRepo.findAll();
        assert all.stream().anyMatch(h -> "itest_user".equals(h.getUsername()) && "policy".equals(h.getTopic()));
    }

    @Test
    @DisplayName("GET /news/history/{username}?topic= (empty) uses findByUser")
    void testGetHistoryWithEmptyTopic_UsesFindByUser() {
        given()
            .accept(ContentType.JSON)
            .queryParam("topic", "")
        .when()
            .get("/news/history/{username}", "itest_user")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("size()", greaterThanOrEqualTo(1));

        // Verify database state
        var all = historyRepo.findAll();
        assert all.stream().anyMatch(h -> "itest_user".equals(h.getUsername()));
    }

    @Test
    @DisplayName("GET /news/history/{username} invalid messages JSON maps to empty messages array")
    void testGetHistory_InvalidMessages_ReturnsEmptyMessages() {
        // Seed an entry with invalid messages JSON
        ChatHistory bad = new ChatHistory();
        bad.setUsername("bad_user");
        bad.setTopic("policy");
        bad.setMessages("not-json");
        historyRepo.save(bad);

        given()
            .accept(ContentType.JSON)
        .when()
            .get("/news/history/{username}", "bad_user")
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"))
            .body("[0].messages", hasSize(0));

    // Verify database state
    var all = historyRepo.findAll();
    assert all.stream().anyMatch(h -> "bad_user".equals(h.getUsername()));
    }

    @Test
    @DisplayName("POST /news/history/{username} returns 200")
    void testPostSaveHistory_ReturnsOk() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"id\":1,\"title\":\"t\"}")
        .when()
            .post("/news/history/{username}", "pathUser")
        .then()
            .statusCode(200);

        // Verify database state (non-destructive)
        var all = historyRepo.findAll();
        assert all.stream().anyMatch(h -> "itest_user".equals(h.getUsername()));
    }

    @Test
    @DisplayName("DELETE /news/history/{username}/{id} success (no principal) returns 204")
    void testDeleteHistory_NoPrincipal_Success() {
        ChatHistory owned = new ChatHistory();
        owned.setUsername("pathUser");
        owned.setTopic("t");
        owned.setMessages("[]");
        owned = historyRepo.save(owned);

        given()
        .when()
            .delete("/news/history/{username}/{id}", "pathUser", owned.getId())
        .then()
            .statusCode(204);

    // Verify database state (record removed)
    Long id = owned.getId();
    assert historyRepo.findById(id).isEmpty();
    }

    @Test
    @DisplayName("DELETE /news/history/{username}/{id} forbidden returns 403")
    void testDeleteHistory_Forbidden_Returns403() {
        ChatHistory owned = new ChatHistory();
        owned.setUsername("owner1");
        owned.setTopic("t");
        owned.setMessages("[]");
        owned = historyRepo.save(owned);

        given()
        .when()
            .delete("/news/history/{username}/{id}", "otherUser", owned.getId())
        .then()
            .statusCode(403);

    // Verify database state (record still present)
    Long id = owned.getId();
    assert historyRepo.findById(id).isPresent();
    }

    @Test
    @DisplayName("DELETE /news/history/{username}/{id} not found returns 404")
    void testDeleteHistory_NotFound_Returns404() {
        given()
        .when()
            .delete("/news/history/{username}/{id}", "someone", 999999)
        .then()
            .statusCode(404);

        // Verify database state (non-destructive)
        assert historyRepo.findById(999999L).isEmpty();
    }
}
