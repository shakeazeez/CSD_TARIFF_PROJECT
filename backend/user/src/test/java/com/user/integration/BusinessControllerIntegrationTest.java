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
import io.restassured.http.ContentType;

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
        
        BusinessDetails detail1 = businessDetailsRepo.save(new BusinessDetails("US", "widgets"));
        BusinessDetails detail2 = businessDetailsRepo.save(new BusinessDetails("MY", "gadgets"));

        
        BusinessUser b = new BusinessUser(
            username,
            "pw",
            Role.BUSINESS,
            new HashSet<>(Set.of(detail1, detail2)),
            "SG"
        );
        b.setTariffData(new HashSet<>(Set.of(detail1, detail2)));
        return userRepo.save(b);
    }

    @Test
    @DisplayName("GET /business/{username} returns BusinessInfoDTO for business user")
    void getBusinessUserDetails_success() {
        preloadBusiness("biz");
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

    @Test
    @DisplayName("POST /business/{username}/entry adds new tariff record")
    void addTariffRecord_success() {
    preloadBusiness("entrybiz");
        // Add a new tariff (not in initial set)
        given()
            .contentType(ContentType.JSON)
            .body(new BusinessTariffDTO("JP", "sprockets"))
        .when()
            .post("/business/{username}/entry", "entrybiz")
        .then()
            .statusCode(200);

        // Verify via GET
        BusinessInfoDTO dto =
            given()
            .when()
                .get("/business/{username}", "entrybiz")
            .then()
                .statusCode(200)
                .extract()
                .as(BusinessInfoDTO.class);
        assert dto.tariffs().size() == 3 : "Expected 3 tariff records after addition";
        List<String> items = dto.tariffs().stream().map(BusinessTariffDTO::item).toList();
        assert items.contains("sprockets") : "New tariff item not present";
    }

    @Test
    @DisplayName("DELETE /business/{username}/entry removes existing tariff record")
    void deleteTariffRecord_success() {
    preloadBusiness("deletebiz");
        // First add a record to be deleted
        given().contentType(ContentType.JSON).body(new BusinessTariffDTO("JP", "widgets2"))
        .when().post("/business/{username}/entry", "deletebiz").then().statusCode(200);

        // Confirm present
        BusinessInfoDTO before =
            given().when().get("/business/{username}", "deletebiz").then().statusCode(200).extract().as(BusinessInfoDTO.class);
        assert before.tariffs().size() == 3 : "Precondition failed: expected 3 tariffs before delete";

        // Delete
        given().contentType(ContentType.JSON).body(new BusinessTariffDTO("JP", "widgets2"))
        .when().delete("/business/{username}/entry", "deletebiz")
        .then().statusCode(200);

        // Verify removal
        BusinessInfoDTO after =
            given().when().get("/business/{username}", "deletebiz").then().statusCode(200).extract().as(BusinessInfoDTO.class);
        assert after.tariffs().size() == 2 : "Expected 2 tariffs after deletion";
        List<String> items = after.tariffs().stream().map(BusinessTariffDTO::item).toList();
        assert !items.contains("widgets2") : "Deleted item still present";
    }

    @Test
    @DisplayName("POST /business/{username}/entry non-business user returns 403")
    void addTariffRecord_forbiddenForNonBusiness() {
        com.user.user.User plain = new com.user.user.User();
        plain.setUsername("notbiz2");
        plain.setHashedPassword("pw");
        plain.setRole(Role.MEMBER);
        userRepo.save(plain);

        given().contentType(ContentType.JSON).body(new BusinessTariffDTO("DE", "bolts"))
        .when().post("/business/{username}/entry", "notbiz2")
        .then().statusCode(403);
    }

    @Test
    @DisplayName("DELETE /business/{username}/entry non-business user returns 403")
    void deleteTariffRecord_forbiddenForNonBusiness() {
        com.user.user.User plain = new com.user.user.User();
        plain.setUsername("notbiz3");
        plain.setHashedPassword("pw");
        plain.setRole(Role.MEMBER);
        userRepo.save(plain);

        given().contentType(ContentType.JSON).body(new BusinessTariffDTO("DE", "bolts"))
        .when().delete("/business/{username}/entry", "notbiz3")
        .then().statusCode(403);
    }
}