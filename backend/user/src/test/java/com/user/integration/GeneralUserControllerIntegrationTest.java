package com.user.integration;

import static io.restassured.RestAssured.given;

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

    @Test
    @DisplayName("GET /user/{username}/csv/ returns full history (>5 entries)")
    void getHistoryDownload_returnsAllEntries() {
        User u = persistUser("csvuser");
        // seed 7 entries with descending counters so order deterministic
        persistHistory(u, 201, 70);
        persistHistory(u, 202, 60);
        persistHistory(u, 203, 50);
        persistHistory(u, 204, 40);
        persistHistory(u, 205, 30);
        persistHistory(u, 206, 20);
        persistHistory(u, 207, 10);

        LinkedHashMap<String, Object> body =
            given()
            .when()
                .get("/user/{username}/csv/", "csvuser")
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<LinkedHashMap<String, Object>>() {});

        assert body.size() == 7 : "Expected all 7 history rows";
        // ordering should be by counter descending -> keys 201..205 then 206 then 207
        assert new ArrayList<>(body.keySet()).equals(Arrays.asList("201","202","203","204","205","206","207")) : "Unexpected ordering in csv history";
    }

    @Test
    @DisplayName("POST /user/{username}/pinned-tariffs/{tariffId} pins up to 3 tariffs and blocks 4th")
    void addPinnedTariff_limitsToThree() {
    persistUser("pinuser");

        // Pin three distinct tariffs
        List<Integer> first =
            given()
            .when()
                .post("/user/{username}/pinned-tariffs/{tariffId}", "pinuser", 900)
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<Integer>>() {});
        assert first.size() == 1 && first.contains(900) : "First pin failed";

        List<Integer> second =
            given()
            .when()
                .post("/user/{username}/pinned-tariffs/{tariffId}", "pinuser", 901)
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<Integer>>() {});
        assert second.size() == 2 && second.containsAll(Arrays.asList(900,901)) : "Second pin failed";

        List<Integer> third =
            given()
            .when()
                .post("/user/{username}/pinned-tariffs/{tariffId}", "pinuser", 902)
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<Integer>>() {});
        assert third.size() == 3 && third.containsAll(Arrays.asList(900,901,902)) : "Third pin failed";

        // Attempt to pin a 4th should yield 409 and not alter list
        given()
        .when()
            .post("/user/{username}/pinned-tariffs/{tariffId}", "pinuser", 903)
        .then()
            .statusCode(409);

        // Verify still 3 via GET /user/{username}
        com.user.dto.UserInfoDTO info =
            given()
            .when()
                .get("/user/{username}", "pinuser")
            .then()
                .statusCode(200)
                .extract()
                .as(com.user.dto.UserInfoDTO.class);
        assert info.pinnedTariffs().size() == 3 : "Pinned list size changed after 409 attempt";
    }

    @Test
    @DisplayName("POST /user/{username}/pinned-tariffs/{tariffId} ignores duplicate pin")
    void addPinnedTariff_duplicateIgnored() {
        persistUser("dupuser");
        List<Integer> list =
            given()
            .when()
                .post("/user/{username}/pinned-tariffs/{tariffId}", "dupuser", 500)
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<Integer>>() {});
        assert list.size() == 1 : "Initial pin should create one entry";

        List<Integer> duplicate =
            given()
            .when()
                .post("/user/{username}/pinned-tariffs/{tariffId}", "dupuser", 500)
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<Integer>>() {});
        assert duplicate.size() == 1 && duplicate.get(0) == 500 : "Duplicate pin should not add another entry";
    }

    @Test
    @DisplayName("POST /user/{username}/unpinned-tariffs/{tariffId} removes pinned tariff")
    void removePinnedTariff_removesSuccessfully() {
        persistUser("unpinuser");
        // Pin two tariffs
        given().when().post("/user/{username}/pinned-tariffs/{tariffId}", "unpinuser", 700).then().statusCode(200);
        given().when().post("/user/{username}/pinned-tariffs/{tariffId}", "unpinuser", 701).then().statusCode(200);

        List<Integer> afterRemoval =
            given()
            .when()
                .post("/user/{username}/unpinned-tariffs/{tariffId}", "unpinuser", 700)
            .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<Integer>>() {});
        assert afterRemoval.size() == 1 && afterRemoval.contains(701) : "Removal did not work as expected";
    }

    @Test
    @DisplayName("GET /user/{username} returns pinned tariffs DTO")
    void getPinnedTariffs_success() {
        persistUser("getpinuser");
        given().when().post("/user/{username}/pinned-tariffs/{tariffId}", "getpinuser", 111).then().statusCode(200);
        given().when().post("/user/{username}/pinned-tariffs/{tariffId}", "getpinuser", 222).then().statusCode(200);

        com.user.dto.UserInfoDTO info =
            given()
            .when()
                .get("/user/{username}", "getpinuser")
            .then()
                .statusCode(200)
                .extract()
                .as(com.user.dto.UserInfoDTO.class);
        assert info.pinnedTariffs().size() == 2 : "Pinned tariffs count mismatch";
        assert info.pinnedTariffs().containsAll(Arrays.asList(111, 222)) : "Pinned tariffs IDs mismatch";
    }

    @Test
    @DisplayName("GET /user/{username}/csv/ unknown user returns 400 (exception caught)")
    void getHistoryDownload_unknownUserReturns400() {
        given()
        .when()
            .get("/user/{username}/csv/", "unknown_csv")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /user/{username}/pinned-tariffs/{tariffId} unknown user returns 400 (exception caught)")
    void addPinnedTariff_unknownUserReturns400() {
        given()
        .when()
            .post("/user/{username}/pinned-tariffs/{tariffId}", "nouser_pin", 123)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /user/{username}/unpinned-tariffs/{tariffId} unknown user returns 400 (exception caught)")
    void removePinnedTariff_unknownUserReturns400() {
        given()
        .when()
            .post("/user/{username}/unpinned-tariffs/{tariffId}", "nouser_unpin", 123)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("GET /user/{username} unknown user returns 404 (exception caught)")
    void getPinnedTariff_unknownUserReturns404() {
        given()
        .when()
            .get("/user/{username}", "nouser_info")
        .then()
            .statusCode(404);
    }
}
