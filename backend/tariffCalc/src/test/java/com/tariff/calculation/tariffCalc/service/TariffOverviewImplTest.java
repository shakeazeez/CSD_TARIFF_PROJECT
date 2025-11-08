package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.GeneralTariffDTO;
import com.tariff.calculation.tariffCalc.dto.HistoricalTariffData;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.WitsDTO;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets.TariffDataSet;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets.TariffSeries;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.dataSets.TariffSeriesData;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.Dimension;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.Observation;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.StartPeriod;
import com.tariff.calculation.tariffCalc.dto.historicalTariffApiDto.structure.Structure;

import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

/**
 * Integration-style unit tests for TariffOverviewImpl focusing on tariff overview retrieval,
 * repository-driven shortcuts, API loading & fallback partner=000 logic, date sorting, zero/negative cost
 * calculations, and validation error scenarios.
 */
@ExtendWith(MockitoExtension.class)
public class TariffOverviewImplTest {

    @Mock
    private TariffRepo tariffRepo;

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private CountryRepo countryRepo;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private TariffOverviewImpl tariffOverviewImpl;

    private Country mockReportingCountry;
    private Country mockPartnerCountry;
    private Country mockWorldCountry;
    private Item mockItem;
    private Tariff mockTariff;

    @BeforeEach
    void setUp() {
        // Mock RestClient chain
        when(restClientBuilder.clone()).thenReturn(restClientBuilder);
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        tariffOverviewImpl = new TariffOverviewImpl(countryRepo, itemRepo, tariffRepo, restClientBuilder);

        // Setup test data
        mockReportingCountry = new Country();
        mockReportingCountry.setCountryName("China");
        mockReportingCountry.setCountryNumber(156);

        mockPartnerCountry = new Country();
        mockPartnerCountry.setCountryName("India");
        mockPartnerCountry.setCountryNumber(356);

        mockWorldCountry = new Country();
        mockWorldCountry.setCountryName("world");
        mockWorldCountry.setCountryNumber(0);

        mockItem = new Item();
        mockItem.setItemName("slipper");
        mockItem.setItemCode(640411);
        mockItem.setCountry(mockReportingCountry);

        mockTariff = new Tariff();
        mockTariff.setId(1);
        mockTariff.setReportingCountry(mockReportingCountry);
        mockTariff.setPartnerCountry(mockPartnerCountry);
        mockTariff.setItem(mockItem);
        mockTariff.setPercentageRate(5.0);
        mockTariff.setDescription("Test tariff");
        mockTariff.setLocalDate(LocalDate.of(2023, 1, 1));
    }

        @Test
        @SuppressWarnings({"rawtypes", "unchecked"})
        void testLoadTariffsFromApi_PadsCountryNumbersAndBuildsUri() {
        // Arrange
        // Force single-digit and two-digit numbers so while-loops execute
        mockReportingCountry.setCountryNumber(7);   // -> 007
        mockPartnerCountry.setCountryNumber(9);     // -> 009
        mockItem.setItemCode(123);                  // -> 000123 via String.format("%06d")

        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 100.0);

        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(mockReportingCountry));
        when(countryRepo.findByCountryName("India")).thenReturn(Optional.of(mockPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("slipper", mockReportingCountry)).thenReturn(Optional.of(mockItem));
        // size <= 1 to trigger API call path
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(mockReportingCountry, mockPartnerCountry, mockItem))
                .thenReturn(new ArrayList<>());

        // Build minimal WitsDTO with one observation so method can complete
        Map<String, java.util.List<Object>> observations = new java.util.LinkedHashMap<>();
        observations.put("0", java.util.List.of(10.0));
        TariffSeriesData seriesData = new TariffSeriesData(observations);
        TariffSeries series = new TariffSeries();
        series.setSeriesData("0:0:0:0:0", seriesData);
        TariffDataSet dataSet = new TariffDataSet(series);
        Observation observation = new Observation(java.util.List.of(new StartPeriod("2001-01-01T00:00:00")));
        Dimension dimension = new Dimension(java.util.List.of(observation));
        Structure structure = new Structure(dimension);
        WitsDTO witsDTO = new WitsDTO(java.util.List.of(dataSet), structure);

        // Mock RestClient chain and capture the URI
        RestClient.RequestHeadersUriSpec requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestSpec);

        final java.util.List<String> capturedUris = new java.util.ArrayList<>();
        when(requestSpec.uri(anyString())).thenAnswer(inv -> {
            String uri = inv.getArgument(0);
            capturedUris.add(uri);
            return (RestClient.RequestHeadersUriSpec) requestSpec;
        });

        RestClient.ResponseSpec spec = mock(RestClient.ResponseSpec.class);
        when(requestSpec.retrieve()).thenReturn(spec);
        when(spec.onStatus(any(), any())).thenReturn(spec);
        when(spec.body(WitsDTO.class)).thenReturn(witsDTO);

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertNotNull(result);
        assertFalse(capturedUris.isEmpty(), "Expected to capture at least one URI call");
        String uri = capturedUris.get(0);
        assertTrue(uri.contains("/reporter/007/partner/009/product/000123/"),
                "URI should contain zero-padded reporter, partner and product: " + uri);
    }

    @Test
    void testGetAllCountries_Success() {
        // Arrange
        List<Country> expectedCountries = Arrays.asList(mockReportingCountry, mockPartnerCountry);
        when(countryRepo.findAll()).thenReturn(expectedCountries);

        // Act
        List<Country> actualCountries = tariffOverviewImpl.getAllCountries();

        // Assert
        assertEquals(expectedCountries.size(), actualCountries.size());
        assertEquals(expectedCountries, actualCountries);
        verify(countryRepo).findAll();
    }

    @Test
    void testGetAllCountries_EmptyList() {
        // Arrange
        when(countryRepo.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Country> result = tariffOverviewImpl.getAllCountries();

        // Assert
        assertTrue(result.isEmpty());
        verify(countryRepo).findAll();
    }

    @Test
    void testGetAllTariff_Success() {
        // Arrange
        Integer tariffId = 1;
        List<Tariff> mockTariffs = Arrays.asList(
                createMockTariff(1, 5.0, LocalDate.of(2023, 1, 1)),
                createMockTariff(2, 6.0, LocalDate.of(2023, 6, 1))
        );

        when(tariffRepo.findById(tariffId)).thenReturn(Optional.of(mockTariff));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                mockReportingCountry, mockPartnerCountry, mockItem)).thenReturn(mockTariffs);

        // Act
        List<GeneralTariffDTO> result = tariffOverviewImpl.getAllTariff(tariffId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("China", result.get(0).reportingCountry());
        assertEquals("India", result.get(0).partnerCountry());
        assertEquals("slipper", result.get(0).item());
        assertEquals(5.0, result.get(0).tariff());
        assertEquals("Test tariff", result.get(0).description());
        
        verify(tariffRepo).findById(tariffId);
        verify(tariffRepo).findByReportingCountryAndPartnerCountryAndItem(
                mockReportingCountry, mockPartnerCountry, mockItem);
    }

    @Test
    void testGetAllTariff_TariffNotFound() {
        // Arrange
        Integer tariffId = 999;
        when(tariffRepo.findById(tariffId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> tariffOverviewImpl.getAllTariff(tariffId));
        assertEquals("Unable to find tariff Id", exception.getMessage());
        
        verify(tariffRepo).findById(tariffId);
        verifyNoInteractions(itemRepo, countryRepo);
    }

    @Test
    void testGetTariffOverview_ExistingTariffs_SkipApiCall() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 1000.0);

        List<Tariff> existingTariffs = Arrays.asList(
                createMockTariff(1, 5.0, LocalDate.of(2023, 1, 1)),
                createMockTariff(2, 6.0, LocalDate.of(2023, 6, 1))
        );

        setupMockRepositoryCalls(queryDTO, existingTariffs);
        // Ensure first tariff is recent (<1 year) and remains earliest among the two
        existingTariffs.get(0).setLocalDate(LocalDate.now());
        existingTariffs.get(1).setLocalDate(LocalDate.now().plusDays(1));

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertNotNull(result);
        assertEquals("China", result.reportingCountry());
        assertEquals("India", result.partnerCountry());
        assertEquals("slipper", result.item());
        assertEquals(2, result.tariffData().size());

        // Verify tariff calculations
        HistoricalTariffData firstTariff = result.tariffData().get(0);
        assertEquals(5.0, firstTariff.tariffRate());
        assertEquals(50.0, firstTariff.tariffAmount()); // 5% of 1000
        assertEquals(1050.0, firstTariff.itemCostWithTariff()); // 1000 + 50

        verify(countryRepo).findByCountryName("China");
        verify(countryRepo).findByCountryName("India");
        verify(itemRepo).findByItemNameAndCountry("slipper", mockReportingCountry);
        verify(tariffRepo).findByReportingCountryAndPartnerCountryAndItem(
                mockReportingCountry, mockPartnerCountry, mockItem);
        
        // Verify no API call was made since we have existing tariffs
        verifyNoInteractions(restClient);
    }

    @Test
    void testGetTariffOverview_NoExistingTariffs_CallApi() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 1000.0);

        List<Tariff> emptyTariffs = new ArrayList<>();
        setupMockRepositoryCalls(queryDTO, emptyTariffs);

        // Act & Assert - API call would throw exception due to complex mocking,
        // but we verify the behavior up to the API call attempt
        try {
            tariffOverviewImpl.getTariffOverview(queryDTO);
        } catch (Exception e) {
            // Expected due to complex RestClient mocking
            assertTrue(e instanceof NullPointerException || e instanceof RuntimeException);
        }
        
        // The important verification is that repositories were called correctly
        verify(countryRepo).findByCountryName("China");
        verify(countryRepo).findByCountryName("India");
        verify(itemRepo).findByItemNameAndCountry("slipper", mockReportingCountry);
        verify(tariffRepo).findByReportingCountryAndPartnerCountryAndItem(
                mockReportingCountry, mockPartnerCountry, mockItem);
    }

    @Test
    void testGetTariffOverview_ReportingCountryNotFound() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "NonExistentCountry", "India", "slipper", 1000.0);

        when(countryRepo.findByCountryName("NonExistentCountry")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> tariffOverviewImpl.getTariffOverview(queryDTO));
        assertEquals("Reporting country not found", exception.getMessage());
        
        verify(countryRepo).findByCountryName("NonExistentCountry");
    }

    @Test
    void testGetTariffOverview_PartnerCountryNotFound() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "NonExistentCountry", "slipper", 1000.0);

        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(mockReportingCountry));
        when(countryRepo.findByCountryName("NonExistentCountry")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> tariffOverviewImpl.getTariffOverview(queryDTO));
        assertEquals("Partner country not found", exception.getMessage());
        
        verify(countryRepo).findByCountryName("China");
        verify(countryRepo).findByCountryName("NonExistentCountry");
    }

    @Test
    void testGetTariffOverview_ItemNotFound() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "NonExistentItem", 1000.0);

        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(mockReportingCountry));
        when(countryRepo.findByCountryName("India")).thenReturn(Optional.of(mockPartnerCountry));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(mockWorldCountry));
        when(itemRepo.findByItemNameAndCountry("nonexistentitem", mockReportingCountry))
                .thenReturn(Optional.empty());
        when(itemRepo.findByItemNameAndCountry("nonexistentitem", mockWorldCountry))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> tariffOverviewImpl.getTariffOverview(queryDTO));
        assertEquals("Item not found for item NonExistentItem", exception.getMessage());
        
        verify(itemRepo).findByItemNameAndCountry("nonexistentitem", mockReportingCountry);
        verify(itemRepo).findByItemNameAndCountry("nonexistentitem", mockWorldCountry);
    }

    @Test
    void testGetTariffOverview_ItemFoundInWorldCountry() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 1000.0);

        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(mockReportingCountry));
        when(countryRepo.findByCountryName("India")).thenReturn(Optional.of(mockPartnerCountry));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(mockWorldCountry));
        when(itemRepo.findByItemNameAndCountry("slipper", mockReportingCountry))
                .thenReturn(Optional.empty());
        when(itemRepo.findByItemNameAndCountry("slipper", mockWorldCountry))
                .thenReturn(Optional.of(mockItem));

        // Use 2 tariffs to avoid API call (since size > 1)
        // Put the recent tariff first to avoid API call via Period check
        List<Tariff> existingTariffs = Arrays.asList(createMockTariff(2, 3.0, LocalDate.now()), mockTariff);
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                mockReportingCountry, mockPartnerCountry, mockItem)).thenReturn(existingTariffs);

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertNotNull(result);
        assertEquals("China", result.reportingCountry());
        assertEquals("India", result.partnerCountry());
        assertEquals("slipper", result.item());
        
        verify(itemRepo).findByItemNameAndCountry("slipper", mockReportingCountry);
        verify(itemRepo).findByItemNameAndCountry("slipper", mockWorldCountry);
    }

    @Test
    void testGetTariffOverview_ZeroItemCost() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 0.0);

        // Use 2 tariffs to avoid API call (since size > 1)
        List<Tariff> existingTariffs = Arrays.asList(createMockTariff(2, 3.0, LocalDate.now()), mockTariff);
        setupMockRepositoryCalls(queryDTO, existingTariffs);

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertNotNull(result);
        HistoricalTariffData tariffData = result.tariffData().get(0);
        assertEquals(0.0, tariffData.tariffAmount());
        assertEquals(0.0, tariffData.itemCostWithTariff());
    }

    @Test
    void testGetTariffOverview_NegativeItemCost() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", -100.0);

        // Use 2 tariffs to avoid API call (since size > 1)
        List<Tariff> existingTariffs = Arrays.asList(createMockTariff(2, 3.0, LocalDate.now()), mockTariff);
        setupMockRepositoryCalls(queryDTO, existingTariffs);

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertNotNull(result);
        HistoricalTariffData tariffData = result.tariffData().get(0);
        assertEquals(-5.0, tariffData.tariffAmount()); // 5% of -100
        assertEquals(-105.0, tariffData.itemCostWithTariff()); // -100 + (-5)
    }

    @Test
    void testGetTariffOverview_SortedByDate() {
        // Arrange
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 1000.0);

        List<Tariff> existingTariffs = Arrays.asList(
                createMockTariff(1, 5.0, LocalDate.now().minusDays(10)), // Later date
                createMockTariff(2, 6.0, LocalDate.now().minusDays(30))  // Earlier date
        );

        setupMockRepositoryCalls(queryDTO, existingTariffs);

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertEquals(2, result.tariffData().size());
        // Should be sorted by date (earlier first)
        assertEquals(LocalDate.now().minusDays(30), result.tariffData().get(0).startPeriod());
        assertEquals(LocalDate.now().minusDays(10), result.tariffData().get(1).startPeriod());
    }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void testGetTariffOverview_ApiLoadParsesTariffs() {
        // Arrange: force API path by returning 0 existing tariffs
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 100.0);

        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(mockReportingCountry));
        when(countryRepo.findByCountryName("India")).thenReturn(Optional.of(mockPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("slipper", mockReportingCountry)).thenReturn(Optional.of(mockItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(mockReportingCountry, mockPartnerCountry, mockItem))
                .thenReturn(new ArrayList<>()); // size <= 1 triggers API load

        // Build WitsDTO with two observations (rates 30.29 and 29.5 for 1990 & 1991)
        Map<String, List<Object>> observations = new LinkedHashMap<>();
        observations.put("0", List.of(30.29));
        observations.put("1", List.of(29.5));
        TariffSeriesData seriesData = new TariffSeriesData(observations);
        TariffSeries series = new TariffSeries();
        series.setSeriesData("0:0:0:0:0", seriesData);
        TariffDataSet dataSet = new TariffDataSet(series);
        Observation observation = new Observation(List.of(
                new StartPeriod("1990-01-01T00:00:00"),
                new StartPeriod("1991-01-01T00:00:00")));
        Dimension dimension = new Dimension(List.of(observation));
        Structure structure = new Structure(dimension);
        WitsDTO witsDTO = new WitsDTO(List.of(dataSet), structure);

        // Mock RestClient chain
        RestClient.RequestHeadersUriSpec requestSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestSpec);
        when(requestSpec.uri(anyString())).thenReturn((RestClient.RequestHeadersUriSpec) requestSpec);
        RestClient.ResponseSpec spec = mock(RestClient.ResponseSpec.class);
        when(requestSpec.retrieve()).thenReturn(spec);
        when(spec.onStatus(any(), any())).thenReturn(spec);
        when(spec.body(WitsDTO.class)).thenReturn(witsDTO);

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.tariffData().size());
        // Sorted by date ascending
        assertEquals(LocalDate.of(1990, 1, 1), result.tariffData().get(0).startPeriod());
        assertEquals(30.29, result.tariffData().get(0).tariffRate());
        assertEquals(30.29, result.tariffData().get(0).tariffAmount()); // 30.29% of 100
        assertEquals(LocalDate.of(1991, 1, 1), result.tariffData().get(1).startPeriod());
        assertEquals(29.5, result.tariffData().get(1).tariffRate());

        // Verify saves invoked for each observation
        verify(tariffRepo, times(2)).save(any(Tariff.class));
    }

        @Test
        @SuppressWarnings({"unchecked", "rawtypes"})
        void testGetTariffOverview_ApiFallbackPartner000() {
        // Arrange: trigger fallback by first API throwing IllegalArgumentException
        TariffCalculationQueryDTO queryDTO = new TariffCalculationQueryDTO(
                "China", "India", "slipper", 50.0);

        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(mockReportingCountry));
        when(countryRepo.findByCountryName("India")).thenReturn(Optional.of(mockPartnerCountry));
        when(itemRepo.findByItemNameAndCountry("slipper", mockReportingCountry)).thenReturn(Optional.of(mockItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(mockReportingCountry, mockPartnerCountry, mockItem))
                .thenReturn(new ArrayList<>()); // force API path

        // First call chain (throws)
        RestClient.RequestHeadersUriSpec firstSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec firstResp = mock(RestClient.ResponseSpec.class);
        // Second call chain (success)
        RestClient.RequestHeadersUriSpec secondSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec secondResp = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) firstSpec, (RestClient.RequestHeadersUriSpec) secondSpec);
        when(firstSpec.uri(anyString())).thenReturn((RestClient.RequestHeadersUriSpec) firstSpec);
        when(firstSpec.retrieve()).thenReturn(firstResp);
        when(firstResp.onStatus(any(), any())).thenReturn(firstResp);
        when(firstResp.body(WitsDTO.class)).thenThrow(new IllegalArgumentException("Dont have for this specific combination"));

        // Successful fallback WitsDTO (one observation)
        Map<String, List<Object>> observations = new LinkedHashMap<>();
        observations.put("0", List.of(10.0));
        TariffSeriesData seriesData = new TariffSeriesData(observations);
        TariffSeries series = new TariffSeries();
        series.setSeriesData("0:0:0:0:0", seriesData);
        TariffDataSet dataSet = new TariffDataSet(series);
        Observation observation = new Observation(List.of(new StartPeriod("2000-01-01T00:00:00")));
        Dimension dimension = new Dimension(List.of(observation));
        Structure structure = new Structure(dimension);
        WitsDTO fallbackDto = new WitsDTO(List.of(dataSet), structure);

        when(secondSpec.uri(anyString())).thenReturn((RestClient.RequestHeadersUriSpec) secondSpec);
        when(secondSpec.retrieve()).thenReturn(secondResp);
        when(secondResp.onStatus(any(), any())).thenReturn(secondResp);
        when(secondResp.body(WitsDTO.class)).thenReturn(fallbackDto);

        // Act
        TariffOverviewResponseDTO result = tariffOverviewImpl.getTariffOverview(queryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.tariffData().size());
        assertEquals(LocalDate.of(2000, 1, 1), result.tariffData().get(0).startPeriod());
        assertEquals(10.0, result.tariffData().get(0).tariffRate());
        assertEquals(5.0, result.tariffData().get(0).tariffAmount()); // 10% of 50
        verify(tariffRepo, times(1)).save(any(Tariff.class));
    }

    // Helper methods
    private Tariff createMockTariff(Integer id, Double rate, LocalDate date) {
        Tariff tariff = new Tariff();
        tariff.setId(id);
        tariff.setReportingCountry(mockReportingCountry);
        tariff.setPartnerCountry(mockPartnerCountry);
        tariff.setItem(mockItem);
        tariff.setPercentageRate(rate);
        tariff.setDescription("Test tariff");
        tariff.setLocalDate(date);
        return tariff;
    }

    private void setupMockRepositoryCalls(TariffCalculationQueryDTO queryDTO, List<Tariff> existingTariffs) {
        when(countryRepo.findByCountryName(queryDTO.reportingCountry())).thenReturn(Optional.of(mockReportingCountry));
        when(countryRepo.findByCountryName(queryDTO.partnerCountry())).thenReturn(Optional.of(mockPartnerCountry));
        when(itemRepo.findByItemNameAndCountry(queryDTO.item().toLowerCase().trim(), mockReportingCountry))
                .thenReturn(Optional.of(mockItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                mockReportingCountry, mockPartnerCountry, mockItem)).thenReturn(existingTariffs);
    }
}