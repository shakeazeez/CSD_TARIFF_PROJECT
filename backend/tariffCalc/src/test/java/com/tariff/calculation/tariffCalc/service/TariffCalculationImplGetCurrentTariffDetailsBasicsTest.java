package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

/**
 * Baseline unit tests for getCurrentTariffDetails method in TariffCalculationImpl, covering
 * happy path, validation failures (missing countries / item), cost edge cases (null, zero, negative),
 * multiple tariff selection (most recent), whitespace normalization and empty tariff list behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TariffCalculation getCurrentTariffDetails Basics")
class TariffCalculationImplGetCurrentTariffDetailsBasicsTest {

    @Mock private CountryRepo countryRepo;
    @Mock private ItemRepo itemRepo;
    @Mock private TariffRepo tariffRepo;
    @Mock private RestClient.Builder restClientBuilder;
    @Mock private RestClient restClient;
    @Mock private ObjectMapper objectMapper;
    @Mock private EmbeddingService embeddingService;

    private TariffCalculationImpl service;

    private Country reporting;
    private Country partner;
    private Item item;
    private Tariff sampleTariff;
    private TariffCalculationQueryDTO query;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.clone()).thenReturn(restClientBuilder);
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        service = new TariffCalculationImpl(countryRepo, itemRepo, tariffRepo, restClientBuilder, objectMapper, embeddingService);

        reporting = new Country();
        reporting.setCountryNumber(840);
        reporting.setCountryName("USA");
        reporting.setCountryCode("US");
        reporting.setIsDeveloping(false);

        partner = new Country();
        partner.setCountryNumber(156);
        partner.setCountryName("China");
        partner.setCountryCode("CN");
        partner.setIsDeveloping(true);

        item = new Item();
        item.setId(1);
        item.setItemCode(123456);
        item.setItemName("electronics");
        item.setCountry(reporting);
        item.setIndustry(Industry.TECHNOLOGY);

        sampleTariff = new Tariff();
        sampleTariff.setId(1);
        sampleTariff.setReportingCountry(reporting);
        sampleTariff.setPartnerCountry(partner);
        sampleTariff.setItem(item);
        sampleTariff.setPercentageRate(15.5);
        sampleTariff.setDescription("Electronics tariff from USA to China");
        sampleTariff.setLocalDate(LocalDate.now());

        query = new TariffCalculationQueryDTO("USA", "China", "Electronics", 1000.0);
    }

    @Test
    @DisplayName("Returns TariffResponseDTO when data exists")
    void returnsTariffResponse_WhenValid() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(sampleTariff));
        // Act
        TariffResponseDTO result = service.getCurrentTariffDetails(query);

        // Assert
        assertNotNull(result);
        assertEquals("USA", result.reportingCountry());
        assertEquals("China", result.partnerCountry());
        assertEquals("electronics", result.item());
        assertEquals(15.5, result.tariffRate());
        assertEquals(155.0, result.tariffAmount());
        assertEquals(1155.0, result.itemCostWithTariff());
        assertEquals(1, result.tariffId());
        assertEquals("Electronics tariff from USA to China", result.tariffDescription());
    }

    @Test
    @DisplayName("Throws when reporting country not found")
    void throwsWhenReportingNotFound() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.getCurrentTariffDetails(query));
    }

    @Test
    @DisplayName("Throws when partner country not found")
    void throwsWhenPartnerNotFound() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.getCurrentTariffDetails(query));
    }

    @Test
    @DisplayName("Handles null item cost")
    void handlesNullItemCost() {
        TariffCalculationQueryDTO q = new TariffCalculationQueryDTO("USA", "China", "Electronics", null);
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(sampleTariff));
        // Act & Assert
        assertThrows(NullPointerException.class, () -> service.getCurrentTariffDetails(q));
    }

    @Test
    @DisplayName("Handles zero item cost")
    void handlesZeroItemCost() {
        TariffCalculationQueryDTO q = new TariffCalculationQueryDTO("USA", "China", "Electronics", 0.0);
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(sampleTariff));
        // Act
        TariffResponseDTO result = service.getCurrentTariffDetails(q);
        // Assert
        assertEquals(0.0, result.tariffAmount());
        assertEquals(0.0, result.itemCostWithTariff());
    }

    @Test
    @DisplayName("Handles negative item cost")
    void handlesNegativeItemCost() {
        TariffCalculationQueryDTO q = new TariffCalculationQueryDTO("USA", "China", "Electronics", -100.0);
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(sampleTariff));
        // Act
        TariffResponseDTO result = service.getCurrentTariffDetails(q);
        // Assert
        assertEquals(-15.5, result.tariffAmount());
        assertEquals(-115.5, result.itemCostWithTariff());
    }

    @Test
    @DisplayName("Returns most recent tariff when multiple exist")
    void returnsMostRecentTariff() {
        // Arrange
        Tariff older = new Tariff(reporting, partner, item, 20.0, "Older", LocalDate.now().minusDays(10));
        older.setId(3);
        Tariff newer = new Tariff(reporting, partner, item, 12.0, "Newer", LocalDate.now().minusDays(1));
        newer.setId(4);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(older, newer));
        // Act
        TariffResponseDTO result = service.getCurrentTariffDetails(query);
        // Assert
        assertEquals(12.0, result.tariffRate());
        assertEquals(4, result.tariffId());
        assertEquals("Newer", result.tariffDescription());
    }

    @Test
    @DisplayName("Handles whitespace in item names")
    void handlesWhitespaceInItemNames() {
        TariffCalculationQueryDTO q = new TariffCalculationQueryDTO("USA", "China", "  Electronics, High-Tech  ", 1000.0);
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("  electronics high-tech  ", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(sampleTariff));
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(q);
        // Assert
        assertNotNull(res);
        verify(itemRepo).findByItemNameAndCountry("  electronics high-tech  ", reporting);
    }

    @Test
    @DisplayName("Throws on null country names")
    void throwsOnNullCountryNames() {
        TariffCalculationQueryDTO q = new TariffCalculationQueryDTO(null, "China", "Electronics", 1000.0);
        // Act & Assert
        assertThrows(Exception.class, () -> service.getCurrentTariffDetails(q));
    }

    @Test
    @DisplayName("Handles empty tariff list (attempts API load path)")
    void handlesEmptyTariffList() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(Collections.emptyList());
        // Act & Assert
        try {
            service.getCurrentTariffDetails(query);
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException || e instanceof com.tariff.calculation.tariffCalc.exception.ApiFailureException);
        }
        verify(countryRepo).findByCountryName("USA");
        verify(countryRepo).findByCountryName("China");
        verify(itemRepo).findByItemNameAndCountry("electronics", reporting);
        verify(tariffRepo).findByReportingCountryAndItem(reporting, item);
    }
}
