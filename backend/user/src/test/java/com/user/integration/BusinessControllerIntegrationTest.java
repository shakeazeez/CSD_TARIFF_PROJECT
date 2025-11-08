package com.user.integration;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.user.dto.BusinessInfoDTO;
import com.user.dto.BusinessTariffDTO;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BusinessDetails;
import com.user.user.BusinessDetailsRepo;
import com.user.user.BusinessUser;
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

/**
 * Integration tests for BusinessController.
 * Covers success DTO return, 404 for unknown user, 403 for non-business user, and plain test endpoint.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Business Controller Integration Tests")
class BusinessControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HistoryRepo historyRepo;

    @Autowired
    private BusinessDetailsRepo businessDetailsRepo;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        historyRepo.deleteAll();
        userRepo.deleteAll();
        businessDetailsRepo.deleteAll();
    }

    private BusinessUser preloadBusiness(String username) {
        // Create and save BusinessDetails first
        BusinessDetails detail1 = new BusinessDetails("US", "widgets");
        BusinessDetails detail2 = new BusinessDetails("MY", "gadgets");
        
        detail1 = businessDetailsRepo.save(detail1);
        detail2 = businessDetailsRepo.save(detail2);

        // Create BusinessUser with the new structure
        BusinessUser b = new BusinessUser(
            username,
            "pw",
            Role.BUSINESS,
            new HashSet<>(Set.of(detail1, detail2)),
            "SG"
        );
        return userRepo.save(b);
    }

    private void preloadHistory(BusinessUser user, int tariffId, int counter) {
        History h = new History(tariffId, user);
        h.setCounter(counter);
        h.setLocalDate(LocalDate.now());
        historyRepo.save(h);
    }

    @Test
    @DisplayName("GET /business/{username} returns BusinessInfoDTO for business user")
    void getBusinessUserDetails_success() {
        BusinessUser u = preloadBusiness("biz");
        preloadHistory(u, 101, 5);
        preloadHistory(u, 102, 2);

        BusinessInfoDTO dto =
            given()
            .when()
                .get("/business/{username}", "biz")
            .then()
                .statusCode(200)
                .extract()
                .as(BusinessInfoDTO.class);

        assert "SG".equals(dto.originCountry()) : "Origin mismatch";
        
        // Check tariffs instead of separate itemsSold and destinationCountries
        assert dto.tariffs() != null : "Tariffs should not be null";
        assert dto.tariffs().size() == 2 : "Expected 2 tariff records";
        
        List<String> reportingCountries = dto.tariffs().stream()
                .map(BusinessTariffDTO::reportingCountry)
                .toList();
        List<String> items = dto.tariffs().stream()
                .map(BusinessTariffDTO::item)
                .toList();
        
        assert reportingCountries.containsAll(List.of("US", "MY")) && reportingCountries.size() == 2 : "Reporting countries mismatch";
        assert items.containsAll(List.of("widgets", "gadgets")) && items.size() == 2 : "Items mismatch";
        assert dto.historyTariffIds().size() <= 5 : "Expected <=5 history entries";
    }

    @Test
    @DisplayName("GET /business/{username} unknown returns 404")
    void getBusinessUserDetails_notFound() {
        given()
        .when()
            .get("/business/{username}", "ghost")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET /business/{username} non-business user returns 403")
    void getBusinessUserDetails_forbidden() {
        // create a non-business user (e.g., MemberUser or plain User)
        com.user.user.User plain = new com.user.user.User();
        plain.setUsername("notbiz");
        plain.setHashedPassword("pw");
        plain.setRole(Role.MEMBER);
        userRepo.save(plain);

        given()
        .when()
            .get("/business/{username}", "notbiz")
        .then()
            .statusCode(403);
    }
}