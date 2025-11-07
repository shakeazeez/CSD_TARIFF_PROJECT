package com.tariff.calculation.tariffCalc.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.service.BankIndustrySearchService;
import com.tariff.calculation.tariffCalc.service.TariffCalculationService;
import com.tariff.calculation.tariffCalc.service.TariffOverviewService;

@WebMvcTest(TariffController.class)
@ActiveProfiles("test")
@DisplayName("Tariff Controller Tests (Mocked Services)")
class TariffControllerIntegrationTests {

    @MockBean
    private BankIndustrySearchService bankIndustrySearchService;

    @MockBean
    private TariffCalculationService tariffCalculationService;

    @MockBean
    private TariffOverviewService tariffOverviewService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(new TariffController(
                tariffCalculationService, 
                tariffOverviewService, 
                bankIndustrySearchService));
        RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("Return list of items for a reporting country and industry")
    void getAllItemsAvailableInTheIndustry_ShouldReturnNonEmptyItemList_WhenValidFilterAndDataAvailable() {
        TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "China",
                "AGRICULTURE",
                "1990-01-01",
                "2020-01-01"
        );

        // Mock the service to return a list
        when(bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO))
                .thenReturn(List.of("iron", "cotton"));

        List<String> response = given()
                .contentType(ContentType.JSON)
                .body(filterDTO)
        .when()
                .post("/tariff/items")
        .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList("$", String.class);

        assertEquals(2, response.size());
        assertEquals("iron", response.get(0));
        assertEquals("cotton", response.get(1));
    }

    @Test
    @DisplayName("Return 404 when no items found for reporting country and industry")
    void getAllItemsAvailableInTheIndustry_ShouldReturn404_WhenDatabaseInvalid() throws Exception {
        TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "China",
                "AGRICULTURE",
                "1990-01-01",
                "2020-01-01"
        );

        when(bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO))
                .thenThrow(new RuntimeException("Encountered error loading details"));

        given()
            .contentType(ContentType.JSON)
            .body(filterDTO)
        .when()
            .post("/tariff/items")
        .then()
            .statusCode(404); // Expect 404 Not Found
    }


}
