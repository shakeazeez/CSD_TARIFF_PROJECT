package com.user.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 

import com.user.dto.BankInfoDTO;
import com.user.enums.Industry;
import com.user.enums.Role;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.MemberUser;
import com.user.user.BankUser;
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
 * Integration tests for BankController.
 * Covers success retrieval, forbidden, not found (unknown username), and test endpoint.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BankControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HistoryRepo historyRepo;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost:" + port;
        historyRepo.deleteAll();
        userRepo.deleteAll();
    }

    private BankUser preloadBankUser(String username) {
        BankUser u = new BankUser(username, "pw", new ArrayList<>(), Industry.FINANCE, "SG");
        u.getRole().add(Role.BANK);
        return userRepo.save(u);
    }

    private void preloadHistory(BankUser user, int tariffId, int counter) {
        History h = new History(tariffId, user);
        h.setCounter(counter);
        h.setLocalDate(LocalDate.now());
        historyRepo.save(h);
    }

    @Test
    @DisplayName("GET /bank/{username} returns BankInfoDTO for bank user")
    void getBankInfo_success() {
        // Given (preload bank user + history)
        BankUser u = preloadBankUser("banky");
        preloadHistory(u, 100, 5);
        preloadHistory(u, 101, 3);
        preloadHistory(u, 102, 7);

        // When / Then
        BankInfoDTO dto =
            given()
            .when()
                .get("/bank/{username}", "banky")
            .then()
                .statusCode(200)
                .extract()
                .as(BankInfoDTO.class);

        assert "FINANCE".equals(dto.industry()) : "Industry mismatch";
        assert "SG".equals(dto.originCountry()) : "Origin country mismatch";
        // historyTariffIds map size <=5, contains counters as keys (per BankServiceImpl logic)
        assert dto.historyTariffIds().size() <= 5 : "Expected <= 5 history entries";
    }

    @Test
    @DisplayName("GET /bank/{username} unknown user returns 404")
    void getBankInfo_notFound() {
        // When / Then
        given()
        .when()
            .get("/bank/{username}", "ghost")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET /bank/{username} for non-bank user returns 403")
    void getBankInfo_forbidden() {
        // Given: preload a non-bank user (MemberUser)
    MemberUser member = new MemberUser("regular", "pw", new ArrayList<>(), new ArrayList<>(List.of(Role.MEMBER)));
        userRepo.save(member);

        // When / Then
        given()
        .when()
            .get("/bank/{username}", "regular")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("GET /bank/test returns 'trolling'")
    void testEndpoint() {
        given()
        .when()
            .get("/bank/test")
        .then()
            .statusCode(200)
            .body(equalTo("trolling"));
    }
}
