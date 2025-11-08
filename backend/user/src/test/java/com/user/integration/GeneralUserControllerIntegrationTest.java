package com.user.integration;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
 

import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.User;
import com.user.user.UserRepo;
import com.user.enums.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.common.mapper.TypeRef; // For type-safe extraction of generic response maps

/**
 * Integration tests for GeneralUserController.
 * 
 * Scope: Exercise HTTP layer + JPA persistence using an in-memory H2 database.
 * 
 * Tested endpoints:
 *  - POST /user/{username}/history/{tariffId}: success & unknown user (400)
 *  - GET  /user/{username}/history: success ordering & unknown user (400)
 *  - GET  /user/testauth/multilevel: simple string response
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("General User Controller Integration Tests")
class GeneralUserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HistoryRepo historyRepo;

    @BeforeEach
    void resetRestAssuredBaseUri() {
        RestAssured.port = port;
        historyRepo.deleteAll();
        userRepo.deleteAll();
    }

    private User persistUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setHashedPassword("pwd");
        u.setRole(Role.MEMBER);
        return userRepo.save(u);
    }

    private History persistHistory(User u, int tariffId, int counter) {
        History h = new History(tariffId, u);
        h.setCounter(counter); // override default 1
        h.setLocalDate(LocalDate.now());
        return historyRepo.save(h);
    }

    @Test
    @DisplayName("POST /user/{username}/history/{tariffId} creates new history entry and returns top 5")
    void addHistory_createsEntryAndReturnsTop5() {
    // preload existing user + history rows
        User u = persistUser("alice");
        // seed >5 existing history entries with varying counters
        persistHistory(u, 1, 10);
        persistHistory(u, 2, 8);
        persistHistory(u, 3, 7);
        persistHistory(u, 4, 5);
        persistHistory(u, 5, 3);
        persistHistory(u, 6, 1);

    LinkedHashMap<String, Object> body =
            given()
                .contentType(ContentType.JSON)
            .when()
                .post("/user/{username}/history/{tariffId}", "alice", 99)
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<LinkedHashMap<String, Object>>() {});

        assert body.size() == 5 : "Expected 5 items in response";
        assert new ArrayList<>(body.keySet()).equals(Arrays.asList("1","2","3","4","5")) : "Unexpected key order";

            // And: DB should contain 7 history rows (6 seeded + 1 new)
        assert historyRepo.count() == 7 : "History row count mismatch";
        assert historyRepo.findByTariffIdAndUser(99, u).isPresent() : "New history row not found";
    }

    @Test
    @DisplayName("POST /user/{username}/history/{tariffId} unknown user returns 400")
    void addHistory_unknownUserReturns400() {
        // no user preloaded

        given()
        .when()
            .post("/user/{username}/history/{tariffId}", "ghost", 50)
        .then()
            .statusCode(400);

        assert historyRepo.count() == 0 : "No history should be created for unknown user";
    }

    @Test
    @DisplayName("GET /user/{username}/history returns top 5 sorted by counter desc")
    void getHistory_returnsTop5SortedDescending() {
    // preload user + >5 history rows for ordering
        User u = persistUser("bob");
        persistHistory(u, 10, 100);
        persistHistory(u, 11, 50);
        persistHistory(u, 12, 30);
        persistHistory(u, 13, 20);
        persistHistory(u, 14, 10);
        persistHistory(u, 15, 1);

    LinkedHashMap<String, Object> body =
            given()
            .when()
                .get("/user/{username}/history", "bob")
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<LinkedHashMap<String, Object>>() {});
        assert body.size() == 5 : "Expected top 5 only";
        assert new ArrayList<>(body.keySet()).equals(Arrays.asList("10","11","12","13","14")) : "Unexpected ordering of keys";
    }

    @Test
    @DisplayName("GET /user/{username}/history unknown user returns 400")
    void getHistory_unknownUserReturns400() {
        given()
        .when()
            .get("/user/{username}/history", "nobody")
        .then()
            .statusCode(400);
    }
}
