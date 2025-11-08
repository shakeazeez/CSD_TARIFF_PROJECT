package com.user.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.user.dto.CreateUserDTO;
import com.user.enums.Role;
import com.user.user.User;
import com.user.history.HistoryRepo;
import com.user.user.UserRepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Integration tests for AuthUserController (registration & simple test endpoint).
 * Uses real service and H2 for success/duplicate flows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Auth User Controller Integration Tests")
class AuthUserControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HistoryRepo historyRepo;

    @BeforeEach
    void setup() {
        RestAssured.port = port;

        historyRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/register registers MEMBER user and returns TokenDTO")
    @org.springframework.transaction.annotation.Transactional
    void registerUser_memberSuccess() {
        CreateUserDTO payload = new CreateUserDTO(
            "newuser", "pw", "MEMBER", null, null, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("username", equalTo("newuser"))
            .body("token", nullValue());

        // And DB contains exactly one MEMBER user with the username
        assert userRepo.count() == 1 : "Expected exactly 1 user";
        var created = userRepo.findByUsername("newuser");
        assert created.isPresent() : "Expected created user to be present";
    
        assert created.get().getRole() == Role.MEMBER : "Expected role MEMBER";
    }

    @Test
    @DisplayName("POST /auth/register with existing username returns 409")
    void registerUser_duplicateConflict() {
        // preload existing user with same username
        User existing = new User();
        existing.setUsername("dup");
        existing.setHashedPassword("x");
        existing.setRole(Role.MEMBER);
        userRepo.save(existing);

        CreateUserDTO payload = new CreateUserDTO(
            "dup", "pw", "MEMBER", null, null, null, null
        );

        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(409);

        // And DB state unchanged (still one preloaded user only)
        assert userRepo.count() == 1 : "DB should still contain only the preloaded user";
        assert userRepo.findByUsername("dup").isPresent() : "Preloaded user 'dup' must still exist";
    }

    @Test
    @DisplayName("POST /auth/register with null role triggers generic exception path -> 400")
    void registerUser_nullRoleReturns400() {
        // payload with null role causes NullPointerException in service (role.toUpperCase())
        CreateUserDTO payload = new CreateUserDTO(
            "boomUser", "pw", null, null, null, null, null
        );

        // controller catches Exception and returns 400
        given()
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400);

        // And DB has no new users created
        assert userRepo.count() == 0 : "No users should be created on bad request";
        assert userRepo.findByUsername("boomUser").isEmpty() : "boomUser must not exist after 400";
    }
}
