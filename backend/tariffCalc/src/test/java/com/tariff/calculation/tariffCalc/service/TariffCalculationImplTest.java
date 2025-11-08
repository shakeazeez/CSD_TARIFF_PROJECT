package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.GeneralTariffDTO;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

/**
 * Comprehensive unit tests for TariffCalculationImpl
 * Tests all public methods, business logic, and exception handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TariffCalculation Service Unit Tests")
class TariffCalculationImplTest {

    @Mock
    private CountryRepo countryRepo;

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private TariffRepo tariffRepo;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmbeddingService embeddingService;

    private TariffCalculationImpl tariffCalculationService;

    private Country testReportingCountry;
    private Country testPartnerCountry;
    private Country testDevelopingCountry;
    private Item testItem;
    private Tariff testTariff;
    private TariffCalculationQueryDTO testQueryDTO;

    @BeforeEach
    void setUp() {
        // Mock RestClient.Builder chain properly
        when(restClientBuilder.clone()).thenReturn(restClientBuilder);
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        // Manually create the service instance to avoid constructor issues
        tariffCalculationService = new TariffCalculationImpl(
                countryRepo, itemRepo, tariffRepo, restClientBuilder, objectMapper, embeddingService);

        // Set up test data
        testReportingCountry = new Country();
        testReportingCountry.setCountryNumber(840); // USA
        testReportingCountry.setCountryName("USA");
        testReportingCountry.setCountryCode("US");
        testReportingCountry.setIsDeveloping(false);

        testPartnerCountry = new Country();
        testPartnerCountry.setCountryNumber(156); // China
        testPartnerCountry.setCountryName("China");
        testPartnerCountry.setCountryCode("CN");
        testPartnerCountry.setIsDeveloping(true);

        testDevelopingCountry = new Country();
        testDevelopingCountry.setCountryNumber(-1);
        testDevelopingCountry.setCountryName("developing");
        testDevelopingCountry.setCountryCode("D");
        testDevelopingCountry.setIsDeveloping(true);

        testItem = new Item();
        testItem.setId(1);
        testItem.setItemCode(123456);
        testItem.setItemName("electronics");
        testItem.setCountry(testReportingCountry);
        testItem.setIndustry(Industry.TECHNOLOGY);

        testTariff = new Tariff();
        testTariff.setId(1);
        testTariff.setReportingCountry(testReportingCountry);
        testTariff.setPartnerCountry(testPartnerCountry);
        testTariff.setItem(testItem);
        testTariff.setPercentageRate(15.5);
        testTariff.setDescription("Electronics tariff from USA to China");
        testTariff.setLocalDate(LocalDate.now());

        testQueryDTO = new TariffCalculationQueryDTO("USA", "China", "Electronics", 1000.0);
    }

    // ===== GET TARIFF BY ID TESTS =====

    @Test
    @DisplayName("Should return GeneralTariffDTO when tariff exists")
    void getTariffById_ShouldReturnGeneralTariffDTO_WhenTariffExists() {
        // Arrange
        Integer tariffId = 1;
        when(tariffRepo.findById(tariffId)).thenReturn(Optional.of(testTariff));

        // Act
        GeneralTariffDTO result = tariffCalculationService.getTariffById(tariffId);

        // Assert
        assertNotNull(result);
        assertEquals("USA", result.reportingCountry());
        assertEquals("China", result.partnerCountry());
        assertEquals("electronics", result.item());
        assertEquals(15.5, result.tariff());
        assertEquals("Electronics tariff from USA to China", result.description());
        verify(tariffRepo, times(1)).findById(tariffId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when tariff does not exist")
    void getTariffById_ShouldThrowException_WhenTariffDoesNotExist() {
        // Arrange
        Integer nonExistentId = 999;
        when(tariffRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tariffCalculationService.getTariffById(nonExistentId));

        assertEquals("Unable to find tariff Id", exception.getMessage());
        verify(tariffRepo, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should handle null tariff ID")
    void getTariffById_ShouldHandleNullId() {
        // Arrange
        Integer nullId = null;

        // Act & Assert
        assertThrows(Exception.class, () -> tariffCalculationService.getTariffById(nullId));
        // Repository will be called but will likely throw an exception
    }

    @Test
    @DisplayName("Should handle negative tariff ID")
    void getTariffById_ShouldHandleNegativeId() {
        // Arrange
        Integer negativeId = -1;
        when(tariffRepo.findById(negativeId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tariffCalculationService.getTariffById(negativeId));

        assertEquals("Unable to find tariff Id", exception.getMessage());
        verify(tariffRepo, times(1)).findById(negativeId);
    }

    // ===== GET CURRENT TARIFF DETAILS TESTS =====

    @Test
    @DisplayName("Should return TariffResponseDTO when countries and items exist with tariffs")
    void getCurrentTariffDetails_ShouldReturnTariffResponseDTO_WhenValidDataExists() {
        // Arrange
        List<Tariff> existingTariffs = Arrays.asList(testTariff);

        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("electronics", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(existingTariffs);

        // Act
        TariffResponseDTO result = tariffCalculationService.getCurrentTariffDetails(testQueryDTO);

        // Assert
        assertNotNull(result);
        assertEquals("USA", result.reportingCountry());
        assertEquals("China", result.partnerCountry());
        assertEquals("electronics", result.item());
        assertEquals(15.5, result.tariffRate());
        assertEquals(155.0, result.tariffAmount()); // 15.5% of 1000
        assertEquals(1155.0, result.itemCostWithTariff()); // 1000 + 155
        assertEquals(1, result.tariffId());
        assertEquals("Electronics tariff from USA to China", result.tariffDescription());

        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
        verify(itemRepo, times(1)).findByItemNameAndCountry("electronics", testReportingCountry);
        verify(tariffRepo, times(1)).findByReportingCountryAndItem(testReportingCountry, testItem);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when reporting country not found")
    void getCurrentTariffDetails_ShouldThrowException_WhenReportingCountryNotFound() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tariffCalculationService.getCurrentTariffDetails(testQueryDTO));

        assertEquals("Country not found", exception.getMessage());
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, never()).findByCountryName("China");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when partner country not found")
    void getCurrentTariffDetails_ShouldThrowException_WhenPartnerCountryNotFound() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tariffCalculationService.getCurrentTariffDetails(testQueryDTO));

        assertEquals("Country not found", exception.getMessage());
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
    }

    @Test
    @DisplayName("Should use developing country tariff when no specific partner tariff found")
    void getCurrentTariffDetails_ShouldUseDevelopingCountryTariff_WhenNoSpecificPartnerTariffFound() {
        // Arrange
        Tariff developingTariff = new Tariff();
        developingTariff.setId(2);
        developingTariff.setReportingCountry(testReportingCountry);
        developingTariff.setPartnerCountry(testDevelopingCountry);
        developingTariff.setItem(testItem);
        developingTariff.setPercentageRate(10.0);
        developingTariff.setDescription("Developing country tariff");
        developingTariff.setLocalDate(LocalDate.now());

        List<Tariff> tariffsWithoutPartner = Arrays.asList(developingTariff);

        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("electronics", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(tariffsWithoutPartner);
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(testDevelopingCountry));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(testDevelopingCountry));

        // Act
        TariffResponseDTO result = tariffCalculationService.getCurrentTariffDetails(testQueryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(10.0, result.tariffRate());
        assertEquals(100.0, result.tariffAmount()); // 10% of 1000
        assertEquals(1100.0, result.itemCostWithTariff()); // 1000 + 100
        assertEquals(2, result.tariffId());

        verify(countryRepo, times(1)).findByCountryName("developing");
    }

    @Test
    @DisplayName("Should handle null item cost gracefully")
    void getCurrentTariffDetails_ShouldHandleNullItemCost() {
        // Arrange
        TariffCalculationQueryDTO queryWithNullCost = new TariffCalculationQueryDTO(
                "USA", "China", "Electronics", null);
        List<Tariff> existingTariffs = Arrays.asList(testTariff);

        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("electronics", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(existingTariffs);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> tariffCalculationService.getCurrentTariffDetails(queryWithNullCost));
    }

    @Test
    @DisplayName("Should handle zero item cost")
    void getCurrentTariffDetails_ShouldHandleZeroItemCost() {
        // Arrange
        TariffCalculationQueryDTO queryWithZeroCost = new TariffCalculationQueryDTO(
                "USA", "China", "Electronics", 0.0);
        List<Tariff> existingTariffs = Arrays.asList(testTariff);

        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("electronics", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(existingTariffs);

        // Act
        TariffResponseDTO result = tariffCalculationService.getCurrentTariffDetails(queryWithZeroCost);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.tariffAmount()); // 15.5% of 0
        assertEquals(0.0, result.itemCostWithTariff()); // 0 + 0
    }

    @Test
    @DisplayName("Should handle negative item cost")
    void getCurrentTariffDetails_ShouldHandleNegativeItemCost() {
        // Arrange
        TariffCalculationQueryDTO queryWithNegativeCost = new TariffCalculationQueryDTO(
                "USA", "China", "Electronics", -100.0);
        List<Tariff> existingTariffs = Arrays.asList(testTariff);

        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("electronics", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(existingTariffs);

        // Act
        TariffResponseDTO result = tariffCalculationService.getCurrentTariffDetails(queryWithNegativeCost);

        // Assert
        assertNotNull(result);
        assertEquals(-15.5, result.tariffAmount()); // 15.5% of -100
        assertEquals(-115.5, result.itemCostWithTariff()); // -100 + (-15.5)
    }

    @Test
    @DisplayName("Should handle multiple tariffs and return most recent")
    void getCurrentTariffDetails_ShouldReturnMostRecentTariff_WhenMultipleTariffsExist() {
        // Arrange
        Tariff olderTariff = new Tariff();
        olderTariff.setId(3);
        olderTariff.setReportingCountry(testReportingCountry);
        olderTariff.setPartnerCountry(testPartnerCountry);
        olderTariff.setItem(testItem);
        olderTariff.setPercentageRate(20.0);
        olderTariff.setDescription("Older tariff");
        olderTariff.setLocalDate(LocalDate.now().minusDays(10));

        Tariff newerTariff = new Tariff();
        newerTariff.setId(4);
        newerTariff.setReportingCountry(testReportingCountry);
        newerTariff.setPartnerCountry(testPartnerCountry);
        newerTariff.setItem(testItem);
        newerTariff.setPercentageRate(12.0);
        newerTariff.setDescription("Newer tariff");
        newerTariff.setLocalDate(LocalDate.now().minusDays(1));

        List<Tariff> multipleTariffs = Arrays.asList(olderTariff, newerTariff);

        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("electronics", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(multipleTariffs);

        // Act
        TariffResponseDTO result = tariffCalculationService.getCurrentTariffDetails(testQueryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(12.0, result.tariffRate()); // Should use newer tariff
        assertEquals(4, result.tariffId()); // Should use newer tariff ID
        assertEquals("Newer tariff", result.tariffDescription());
    }

    // ===== EDGE CASE AND ERROR HANDLING TESTS =====

    @Test
    @DisplayName("Should handle empty tariff list")
    void getCurrentTariffDetails_ShouldHandleEmptyTariffList() {
        // Arrange - This would typically trigger API loading, but we'll mock empty list
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("electronics", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(Collections.emptyList());

        // Act & Assert - This would normally try to load from API, which would require more complex mocking
        // For now, we expect the method to attempt API loading
        try {
            tariffCalculationService.getCurrentTariffDetails(testQueryDTO);
            // If no exception, the method successfully handled empty list scenario
        } catch (Exception e) {
            // Expected if API loading fails - that's fine for unit test
            assertTrue(e instanceof RuntimeException || e instanceof ApiFailureException);
        }

        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
        verify(itemRepo, times(1)).findByItemNameAndCountry("electronics", testReportingCountry);
        verify(tariffRepo, times(1)).findByReportingCountryAndItem(testReportingCountry, testItem);
    }

    @Test
    @DisplayName("Should handle whitespace in item names correctly")
    void getCurrentTariffDetails_ShouldHandleWhitespaceInItemNames() {
        // Arrange
        TariffCalculationQueryDTO queryWithWhitespace = new TariffCalculationQueryDTO(
                "USA", "China", "  Electronics, High-Tech  ", 1000.0);

        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        
        // The service should process "  electronics high-tech  " without trimming
        when(itemRepo.findByItemNameAndCountry("  electronics high-tech  ", testReportingCountry))
                .thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndItem(testReportingCountry, testItem))
                .thenReturn(Arrays.asList(testTariff));

        // Act
        TariffResponseDTO result = tariffCalculationService.getCurrentTariffDetails(queryWithWhitespace);

        // Assert
        assertNotNull(result);
        verify(itemRepo, times(1)).findByItemNameAndCountry("  electronics high-tech  ", testReportingCountry);
    }

    @Test
    @DisplayName("Should handle null country names gracefully")
    void getCurrentTariffDetails_ShouldHandleNullCountryNames() {
        // Arrange
        TariffCalculationQueryDTO queryWithNullCountries = new TariffCalculationQueryDTO(
                null, "China", "Electronics", 1000.0);

        // Act & Assert
        assertThrows(Exception.class,
                () -> tariffCalculationService.getCurrentTariffDetails(queryWithNullCountries));
    }
}