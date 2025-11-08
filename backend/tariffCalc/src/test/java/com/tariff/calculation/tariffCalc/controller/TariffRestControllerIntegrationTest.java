package com.tariff.calculation.tariffCalc.controller;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.tariff.calculation.tariffCalc.category.Category;
import com.tariff.calculation.tariffCalc.category.CategoryRepo;
import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.GeneralTariffDTO;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.SelectedItemsDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffDetailsforItemDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.service.EmbeddingService;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Book Controller Real Server Integration Tests")
class TariffRestControllerIntegrationTest {
    
    @LocalServerPort
    private int port;

    @Autowired
    private TariffRepo tariffRepo;

    @Autowired
    private ItemRepo itemRepo;

    @Autowired
    private CountryRepo countryRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @MockBean
    private EmbeddingService embeddingService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        clearDatabase();
        insertTariffRepoData();
        stubEmbedding();
    }

    private void clearDatabase() {
    // Clear join tables and child records first
    itemRepo.findAll().forEach(item -> {
        item.setTariffs(null);
        itemRepo.save(item);
    });
    
    countryRepo.findAll().forEach(country -> {
        country.setPartnerTariff(null);
        country.setReportingTariff(null);
        country.setItemsStored(null);
        countryRepo.save(country);
    });

    // Now delete main entities in correct order
    tariffRepo.deleteAll();
    itemRepo.deleteAll();
    countryRepo.deleteAll();
    categoryRepo.deleteAll();
    }

    private void insertTariffRepoData() {
        // Create category with test embeddings 
        Category category = new Category();
        category.setName("TEST_CATEGORY");
        category.setDesc("TEST_DESCRIPTION");
        category.setEmbedding(new float[]{0.1f, 0.2f, 0.3f});
        categoryRepo.save(category);

        // Create and save countries first
        Country china = new Country(1, "100", "China", false, null, null, null);
        Country india = new Country(2, "200", "India", true, null, null, null);
        countryRepo.saveAll(List.of(china,india));

        china = countryRepo.findByCountryName("China").get();
        india = countryRepo.findByCountryName("India").get();

        // Create and save item
        Item item1 = new Item(null,1, "iron", null, china, Industry.AGRICULTURE);
        itemRepo.save(item1);

        // Create and save tariffs
        Tariff t1 = new Tariff(null, china, india, item1, 5.0, "Tariff Description", LocalDate.of(2001, 1, 1));
        Tariff t2 = new Tariff(null, china, india, item1, 5.0, "Tariff Description", LocalDate.of(2002, 1, 1));
        Tariff t3 = new Tariff(null, china, india, item1, 5.0, "Tariff Description", LocalDate.of(2003, 1,1 ));
        tariffRepo.saveAll(List.of(t1, t2, t3));

        // Update related informations 
        item1.setTariffs(List.of(t1, t2, t3));
        itemRepo.save(item1);

        china.setPartnerTariff(List.of(t1, t2, t3));
        china.setItemsStored(List.of(item1));

        india.setReportingTariff(List.of(t1, t2, t3));
        india.setItemsStored(List.of(item1));

        // Save updated entities
        countryRepo.saveAll(List.of(china, india));
        china = countryRepo.findByCountryName("China").get();
        india = countryRepo.findByCountryName("India").get();
    }

    @BeforeEach
    void stubEmbedding() {
        when(embeddingService.getEmbedding(anyString()))
            .thenReturn(new float[]{0.1f, 0.2f, 0.3f});
    }

    @Test
    @DisplayName("GET /countries - Returns list of countries when data exists")
    void getAllCountries_ReturnsListOfAllCountries_WhenDataExists() {

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/tariff/countries")
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("countryCode", hasItems("100", "200"))
            .body("countryName", hasItems("China", "India"))
            .body("isDeveloping", hasItems(true, false));     
    }


    @Test
    @DisplayName("GET /countries - Returns list of countries when data exists")
    void getAllIndustries_ReturnsListOfAllCountries_WhenDataExists() {
        
        String[] expectedIndustries = Arrays.stream(Industry.values()).map(Industry::toString).toArray(String[]::new);

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/tariff/industries")
        .then()
            .statusCode(200)
            .body("$", hasSize(21))
            .body("$", hasItems(expectedIndustries));    
    }


    @Test
    @DisplayName("GET /industries - Returns 404 when Industry lookup fails")
    void getAllIndustries_ShouldReturn404_WhenIndustryLookupFails() {
        // Mock the Industry enum to return null
        when(embeddingService.getEmbedding("INVALID_INDUSTRY"))
            .thenReturn(null);

        given()
            .contentType(ContentType.JSON)
            .queryParam("industry", "INVALID_INDUSTRY")
        .when()
            .get("/tariff/industries")
        .then()
            .statusCode(404)
            .body(emptyOrNullString());
    }

    @Test
    @DisplayName("Return list of items for a reporting country and industry with valid tariff information")
    void getAllItemsAvailableInTheIndustry_ShouldReturnNonEmptyItemList_WhenValidFilterAndDataAvailable() {
        
        TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
            "China",
            "AGRICULTURE",
            "1990-01-01",
            "2020-01-01"
        );

        // Get response and extract as List
        List<String> responseItems = given()
            .contentType(ContentType.JSON)
            .body(filterDTO)
        .when()
            .post("/tariff/items")
        .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath().getList("$", String.class);

        // Verify the response list
        assertNotNull(responseItems, "Response list should not be null");
        assertFalse(responseItems.isEmpty(), "Response list should not be empty");
        assertTrue(responseItems.contains("iron"), "Response should contain 'iron'");
        
        var dbItems = itemRepo.findAll();
        assertEquals(dbItems.size(), responseItems.size(), "Response size should match DB size");
    }

    @Test
    @DisplayName("Return Not Found if there the database throws an exception not being able to get tariff details")
    void getAllItemsAvailableInTheIndustry_ShouldReturnHttpStatusNotFound_WhenDatabaseCannotFindItemForIndustry() {
        TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
            "China",
            "DOES_NOT_EXIST_AHH_INDUSTRY",
            "1990-01-01",
            "2020-01-01"
        );

        given()
            .contentType(ContentType.JSON)
            .body(filterDTO)
        .when()
            .post("/tariff/items")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("POST /items/tariffDetails - Returns tariff details when valid data exists")
    void getTariffDetailsForItem_ShouldReturnDetails_WhenValidDataExists() {
        SelectedItemsDTO requestDTO = new SelectedItemsDTO(
                "iron", // selectedItem
                "China", // homeCountry
                "AGRICULTURE", // industry
                "1990-01-01", // startDate
                "2020-01-01" // endDate
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/tariff/items/tariffDetails")
                .then()
                .statusCode(200)
                .body("itemName", equalTo("iron"))
                .body("hscode", equalTo(1))
                .body("tariffDetailsList", hasSize(greaterThan(0)))
                .body("tariffDetailsList[0].country.countryName", equalTo("India"));
    }

    @Test
    @DisplayName("POST /items/tariffDetails - Returns 404 when item not found")
    void getTariffDetailsForItem_ShouldReturn404_WhenItemNotFound() {
        SelectedItemsDTO requestDTO = new SelectedItemsDTO(
                "nonexistent_item", // selectedItem
                "China", // homeCountry
                "AGRICULTURE", // industry
                "1990-01-01", // startDate
                "2020-01-01" // endDate
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/tariff/items/tariffDetails")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /items/tariffDetails - Returns 404 when country not found")
    void getTariffDetailsForItem_ShouldReturn404_WhenCountryNotFound() {
        SelectedItemsDTO requestDTO = new SelectedItemsDTO(
                "iron", // selectedItem
                "NonexistentCountry", // homeCountry
                "AGRICULTURE", // industry
                "1990-01-01", // startDate
                "2020-01-01" // endDate
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/tariff/items/tariffDetails")
                .then()
                .statusCode(404);
    }

    // @Test
    // @DisplayName("POST /current - Returns tariff details when valid query provided")
    // void getCurrentTariffDetails_ShouldReturnDetails_WhenValidQueryProvidedAndDataExist() {

        

    //     TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
    //         "China",       
    //         "India",        
    //         "iron",        
    //         1000.0         
    //     );

    //     given()
    //         .contentType(ContentType.JSON)
    //         .body(queryDTO)
    //     .when()
    //         .post("/tariff/current")
    //     .then()
    //         .statusCode(200)
    //         .body("reportingCountry", equalTo("China"))
    //         .body("partnerCountry", equalTo("India"))
    //         .body("item", equalTo("iron"))
    //         .body("itemCost", equalTo(1000.0f))
    //         .body("tariffRate", notNullValue())
    //         .body("finalCost", notNullValue())
    //         .body("finalCost", equalTo(1050.0f));
    // }

    @Test
    @DisplayName("POST /current - Returns 400 when invalid parameters provided")
    void getCurrentTariffDetails_ShouldReturn400_WhenInvalidParametersProvided() {
        TariffCalculationQueryDTO invalidDTO = new TariffCalculationQueryDTO(
            "",             // empty reporting country
            "India",
            "iron",
            -1000.0        // negative cost
        );

        given()
            .contentType(ContentType.JSON)
            .body(invalidDTO)
        .when()
            .post("/tariff/current")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /current - Returns 404 when item not found")
    void getCurrentTariffDetails_ShouldReturn404_WhenItemNotFound() {
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
            "China",
            "India",
            "nonexistent_item",
            1000.0
        );

        given()
            .contentType(ContentType.JSON)
            .body(queryDTO)
        .when()
            .post("/tariff/current")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("POST /current - Returns 500 for unexpected errors")
    void getCurrentTariffDetails_ShouldReturn500_WhenUnexpectedErrorOccurs() {
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
            "China",
            "India",
            null,          
            1000.0
        );

        given()
            .contentType(ContentType.JSON)
            .body(queryDTO)
        .when()
            .post("/tariff/current")
        .then()
            .statusCode(500);
    }

    @Test
    @DisplayName("POST /past - Returns 404 when API call fails")
    void getHistoricalTariffDetails_ShouldReturn404_WhenApiCallFails() {
        // Create test data that will trigger API call
        Country testCountry = new Country(999, "999", "TestCountry", false, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        countryRepo.save(testCountry);
        
        Item testItem = new Item(null, 999999, "testItem", new ArrayList<>(), testCountry, Industry.AGRICULTURE);
        itemRepo.save(testItem);
        
        // This combination of parameters should trigger an API call that fails
        // (invalid country number + non-existent item code)
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
            "TestCountry",    // reporting country that exists but has no tariffs
            "India",         // existing partner
            null,      // item that exists but has no tariffs
            1000.0
        );

        given()
            .contentType(ContentType.JSON)
            .body(queryDTO)
        .when()
            .post("/tariff/past")
        .then()
            .statusCode(500);  // ApiFailureException maps to 404 in controller
    }

    @Test
    @DisplayName("POST /past - Returns 400 when invalid parameters provided")
    void getHistoricalTariffDetails_ShouldReturn400_WhenInvalidParametersProvided() {
        TariffCalculationQueryDTO invalidDTO = new TariffCalculationQueryDTO(
            "",             // empty reporting country (invalid)
            "India",
            "iron",
            -1000.0        // negative cost (invalid)
        );

        given()
            .contentType(ContentType.JSON)
            .body(invalidDTO)
        .when()
            .post("/tariff/past")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /past - Returns 404 when item not found")
    void getHistoricalTariffDetails_ShouldReturn404_WhenItemNotFound() {
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
            "China",
            "India",
            "nonexistent_item",  // item that doesn't exist
            1000.0
        );

        given()
            .contentType(ContentType.JSON)
            .body(queryDTO)
        .when()
            .post("/tariff/past")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("POST /past - Returns 400 when country is invalid")
    void getHistoricalTariffDetails_ShouldReturn400_WhenInvalidCountry() {
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
            "NonexistentCountry",  // country that doesn't exist
            "India",
            "iron",
            1000.0
        );

        given()
            .contentType(ContentType.JSON)
            .body(queryDTO)
        .when()
            .post("/tariff/past")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("GET /current/{id} - Returns tariff details when valid ID exists")
    void getCurrentTariffById_ShouldReturnDetails_WhenValidIdExists() {
        // Get an existing tariff ID from our test data
        Tariff testTariff = tariffRepo.findAll().get(0);
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/tariff/current/" + testTariff.getId())
        .then()
            .statusCode(200)
            .body("reportingCountry", equalTo("China"))
            .body("partnerCountry", equalTo("India"))
            .body("item", equalTo("iron"))
            .body("tariff", equalTo(5.0f))
            .body("description", notNullValue());
    }

    @Test
    @DisplayName("GET /current/{id} - Returns 400 when invalid ID provided")
    void getCurrentTariffById_ShouldReturn400_WhenInvalidIdProvided() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/tariff/current/-1")  // Invalid ID
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("GET /current/{id} - Returns 500 when database error occurs")
    void getCurrentTariffById_ShouldReturn500_WhenDatabaseError() {
        // First save a tariff we can use
        Tariff testTariff = tariffRepo.findAll().get(0);
        Integer tariffId = testTariff.getId();
        
        // Corrupt related entities to cause a DB error while keeping the ID valid
        testTariff.setReportingCountry(null);
        testTariff.setPartnerCountry(null);
        tariffRepo.save(testTariff);
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/tariff/current/" + tariffId)
        .then()
            .statusCode(500);
    }

    @Test
    @DisplayName("POST /past/{id} - Returns historical tariff list when valid ID exists")
    void getPastTariffById_ShouldReturnTariffList_WhenValidIdExists() {
        // Get an existing tariff ID from test data
        Tariff testTariff = tariffRepo.findAll().get(0);
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/tariff/past/" + testTariff.getId())
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].reportingCountry", equalTo("China"))
            .body("[0].partnerCountry", equalTo("India"))
            .body("[0].item", equalTo("iron"))
            .body("[0].tariff", equalTo(5.0f));
    }

    @Test
    @DisplayName("POST /past/{id} - Returns 400 when invalid ID provided")
    void getPastTariffById_ShouldReturn400_WhenInvalidIdProvided() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/tariff/past/-1")  // Invalid negative ID
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /past/{id} - Returns 500 when database error occurs")
    void getPastTariffById_ShouldReturn500_WhenDatabaseError() {
        // First save a tariff we can use
        Tariff testTariff = tariffRepo.findAll().get(0);
        Integer tariffId = testTariff.getId();
        
        // Corrupt related entities to cause a DB error while keeping the ID valid
        testTariff.setReportingCountry(null);
        testTariff.setPartnerCountry(null);
        tariffRepo.save(testTariff);
        
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/tariff/past/" + tariffId)
        .then()
            .statusCode(500);
    }
}
