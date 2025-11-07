package com.user.integration;

import static io.restassured.RestAssured.given;


import java.util.ArrayList;
import java.util.List;

import com.user.enums.Role;
import com.user.dto.MemberInfoDTO;
import com.user.user.MemberUser;
import com.user.user.MemberUserRepo;
import com.user.user.UserRepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;

/**
 * Integration tests for MemberController.
 * Covers add/remove pinned tariff flows, capacity limit, bad user, and role mismatch (403).
 * Data is preloaded per test; H2 DB is reset before each scenario.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MemberControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MemberUserRepo memberUserRepo;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port;
        memberUserRepo.deleteAll();
        userRepo.deleteAll();
    }

    private MemberUser preloadMember(String username, List<Integer> pinned) {
        MemberUser m = new MemberUser(username, "pw", pinned, new ArrayList<>(List.of(Role.MEMBER)));
        return memberUserRepo.save(m);
    }

    @Test
    @DisplayName("POST /member/{username}/pinned-tariffs/{tariffId} adds tariff when <3 pinned")
    void addPinned_success() {
        // Given
        preloadMember("mem", new ArrayList<>());

        // When / Then
        List<?> body =
            given()
            .when()
                .post("/member/{username}/pinned-tariffs/{tariffId}", "mem", 10)
            .then()
                .statusCode(200)
                .extract()
                .as(List.class);

        assert body.equals(List.of(10)) : "Pinned list should contain only 10";
    }

    @Test
    @DisplayName("POST /member/{username}/pinned-tariffs/{tariffId} returns 409 when already 3 pinned")
    void addPinned_capacityLimit() {
        // Given
        preloadMember("mem", new ArrayList<>(List.of(1,2,3)));

        // When / Then
        given()
        .when()
            .post("/member/{username}/pinned-tariffs/{tariffId}", "mem", 4)
        .then()
            .statusCode(409);
    }

    @Test
    @DisplayName("POST /member/{username}/pinned-tariffs/{tariffId} unknown user returns 400")
    void addPinned_unknownUser() {
        given()
        .when()
            .post("/member/{username}/pinned-tariffs/{tariffId}", "ghost", 99)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /member/{username}/unpinned-tariffs/{tariffId} removes existing pinned tariff")
    void removePinned_success() {
        // Given
        preloadMember("mem", new ArrayList<>(List.of(5,6)));

        // When / Then
        List<?> body =
            given()
            .when()
                .post("/member/{username}/unpinned-tariffs/{tariffId}", "mem", 5)
            .then()
                .statusCode(200)
                .extract()
                .as(List.class);

        assert body.equals(List.of(6)) : "Pinned list should contain only 6";
    }

    @Test
    @DisplayName("POST /member/{username}/unpinned-tariffs/{tariffId} unknown user returns 400")
    void removePinned_unknownUser() {
        given()
        .when()
            .post("/member/{username}/unpinned-tariffs/{tariffId}", "ghost", 5)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("GET /member/{username} non-member returns 403")
    void getPinned_forbidden() {
        // Given: create some other user type (no MemberUser). For simplicity just leave DB empty; service will 404 user before 403, so we simulate by creating a non-member user? Simplify: create generic User with MEMBER role missing pinned list -> still not MemberUser, expect 403 via IllegalAccessError path? Actually service casts; if not MemberUser -> IllegalAccessError -> 403.
        // We'll create a plain user via userRepo
        com.user.user.User generic = new com.user.user.User();
        generic.setUsername("plain");
        generic.setHashedPassword("pw");
        generic.setRole(new ArrayList<>(List.of(Role.ADMIN))); // not MEMBER
        userRepo.save(generic);

        given()
        .when()
            .get("/member/{username}", "plain")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("GET /member/{username} unknown user returns 404")
    void getPinned_notFound() {
        given()
        .when()
            .get("/member/{username}", "ghost")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET /member/{username} success returns MemberInfoDTO")
    void getPinned_success() {
        // Given
        preloadMember("mem", new ArrayList<>(List.of(7, 8)));

        // When / Then
        MemberInfoDTO dto =
            given()
            .when()
                .get("/member/{username}", "mem")
            .then()
                .statusCode(200)
                .extract()
                .as(MemberInfoDTO.class);

        assert dto.pinnedTariffs().equals(List.of(7,8)) : "Pinned tariffs mismatch";
    }
}
