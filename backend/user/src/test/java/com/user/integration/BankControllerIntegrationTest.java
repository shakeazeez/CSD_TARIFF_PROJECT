package com.user.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
 
 

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
@DisplayName("Bank Controller Integration Tests")
class BankControllerIntegrationTest {

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

    private BankUser preloadBankUser(String username) {
        BankUser u = new BankUser(username, "pw", Role.BANK, Industry.FINANCE, "SG");
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
    void getBankUserDetails_success() {
        // preload bank user + history
        BankUser u = preloadBankUser("banky");
        preloadHistory(u, 100, 5);
        preloadHistory(u, 101, 3);
        preloadHistory(u, 102, 7);

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
    void getBankUserDetails_notFound() {
        given()
        .when()
            .get("/bank/{username}", "ghost")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET /bank/{username} for non-bank user returns 403")
    void getBankUserDetails_forbidden() {
        // preload a non-bank user with MEMBER role
        MemberUser member = new MemberUser("regular", "pw", Role.MEMBER);
        userRepo.save(member);

        given()
        .when()
            .get("/bank/{username}", "regular")
        .then()
            .statusCode(403);
    }
}
