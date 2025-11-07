package com.user.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.User;
import com.user.user.UserRepo;
import com.user.enums.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Integration tests for GeneralUserController.
 * 
 * Scope: Exercise HTTP layer + JPA persistence using an in-memory H2 database.
 * 
 * Tested endpoints:
 *  - POST /user/{username}/history/{tariffId}: success & unknown user (400)
 *  - GET  /user/{username}/history: success ordering & unknown user (400)
 *  - GET  /user/testauth/multilevel: simple string response
 * 
 * Data Preload Strategy:
 *  Each test preloads only the data it needs to remain isolated and non-destructive. No shared mutation across tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class GeneralUserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HistoryRepo historyRepo;

    @BeforeEach
    void resetRestAssuredBaseUri() {
        RestAssured.baseURI = "http://localhost:" + port;
        historyRepo.deleteAll();
        userRepo.deleteAll();
    }

    private User persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setHashedPassword("pwd");
        u.setRole(new ArrayList<>(List.of(Role.MEMBER)));
        return userRepo.save(u);
    }

    private History persistHistory(User u, int tariffId, int counter) {
        History h = new History(tariffId, u);
        h.setCounter(counter); // override default 1
        h.setLocalDate(LocalDate.now());
        return historyRepo.save(h);
    }

    @Test
    @Order(1)
    @DisplayName("POST /user/{username}/history/{tariffId} creates new history entry and returns top 5")
    void addHistory_success() {
    // Given (preload existing user + history rows)
        User u = persistUser("alice");
        // seed >5 existing history entries with varying counters
        persistHistory(u, 1, 10);
        persistHistory(u, 2, 8);
        persistHistory(u, 3, 7);
        persistHistory(u, 4, 5);
        persistHistory(u, 5, 3);
        persistHistory(u, 6, 1);

        // When / Then
        LinkedHashMap<String, Object> body =
            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/user/{username}/history/{tariffId}", "alice", 99)
            .then()
                .statusCode(200)
                .extract()
                .as(LinkedHashMap.class);

        assert body.size() == 5 : "Expected 5 items in response";
        assert new ArrayList<>(body.keySet()).equals(Arrays.asList("1","2","3","4","5")) : "Unexpected key order";

            // And: DB should contain 7 history rows (6 seeded + 1 new)
        assert historyRepo.count() == 7 : "History row count mismatch";
        assert historyRepo.findByTariffIdAndUser(99, u).isPresent() : "New history row not found";
    }

    @Test
    @Order(2)
    @DisplayName("POST /user/{username}/history/{tariffId} unknown user returns 400")
    void addHistory_unknownUser() {
    // Given: no user preloaded

        // When / Then
        given()
        .when()
            .post("/user/{username}/history/{tariffId}", "ghost", 50)
        .then()
            .statusCode(400);

        assert historyRepo.count() == 0 : "No history should be created for unknown user";
    }

    @Test
    @Order(3)
    @DisplayName("GET /user/{username}/history returns top 5 sorted by counter desc")
    void getHistory_successOrdering() {
    // Given (preload user + >5 history rows for ordering)
        User u = persistUser("bob");
        persistHistory(u, 10, 100);
        persistHistory(u, 11, 50);
        persistHistory(u, 12, 30);
        persistHistory(u, 13, 20);
        persistHistory(u, 14, 10);
        persistHistory(u, 15, 1);

        // When / Then
        LinkedHashMap<String, Object> body =
            given()
            .when()
                .get("/user/{username}/history", "bob")
            .then()
                .statusCode(200)
                .extract()
                .as(LinkedHashMap.class);
        assert body.size() == 5 : "Expected top 5 only";
        assert new ArrayList<>(body.keySet()).equals(Arrays.asList("10","11","12","13","14")) : "Unexpected ordering of keys";
    }

    @Test
    @Order(4)
    @DisplayName("GET /user/{username}/history unknown user returns 400")
    void getHistory_unknownUser() {
        // When / Then
        given()
        .when()
            .get("/user/{username}/history", "nobody")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(5)
    @DisplayName("GET /user/testauth/multilevel returns string body")
    void testAuth_endpoint() {
        // When / Then
        given()
        .when()
            .get("/user/testauth/multilevel")
        .then()
            .statusCode(200)
            .body(equalTo("Hello from authenticated"));
    }
}
