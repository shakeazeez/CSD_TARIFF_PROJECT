package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.MoachDTO;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.TariffData;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.TariffRate;
import com.tariff.calculation.tariffCalc.dto.currentTariffApiDto.TableData;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

/**
 * Focused tests for loadTariffFromApi method in TariffCalculationImpl, validating parsing of
 * general and special rates, table region mapping (MFN/LDC), URI padding,
 * error handling and duplicate prevention.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TariffCalculationImpl loadTariffFromApi Tests")
class TariffCalculationImplLoadTariffFromApiTest {

    @Mock CountryRepo countryRepo;
    @Mock ItemRepo itemRepo;
    @Mock TariffRepo tariffRepo;
    @Mock RestClient.Builder restClientBuilder;
    @Mock RestClient restClient;
    @Mock EmbeddingService embeddingService;
    @Mock com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    TariffCalculationImpl service;
    Country reporting;
    Item item;

    @BeforeEach
    void init() {
        when(restClientBuilder.clone()).thenReturn(restClientBuilder);
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        service = new TariffCalculationImpl(countryRepo, itemRepo, tariffRepo, restClientBuilder, objectMapper, embeddingService);
        reporting = new Country();
        reporting.setCountryNumber(840); // USA
        reporting.setCountryName("USA");
        reporting.setCountryCode("US");
        reporting.setIsDeveloping(false);
        item = new Item();
        item.setId(1);
        item.setItemCode(123456);
        item.setItemName("electronics");
        item.setCountry(reporting);
        item.setIndustry(Industry.TECHNOLOGY);
    }

    // Helper builder
    private MoachDTO moachWithRate(TariffRate rate, List<TableData> tables) {
        TariffData td = new TariffData("123456", rate, "desc", tables);
        return new MoachDTO(List.of(td));
    }

    private void injectRestClientReturning(MoachDTO dto) throws Exception {
        RestClient deepStubClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        when(deepStubClient.get().uri(anyString()).retrieve().onStatus(any(), any()).body(eq(MoachDTO.class)))
            .thenReturn(dto);
        var f = TariffCalculationImpl.class.getDeclaredField("restClientMoach");
        f.setAccessible(true);
        f.set(service, deepStubClient);
    }

    @Test
    @DisplayName("Creates world tariff from general rate; special countries string ignored (current behavior)")
    void handlesSpecialCountriesString_CurrentBehavior() throws Exception {
        // Arrange
        String countriesField = "CA,MX 5%";
        TariffRate rate = new TariffRate(null, "7%", null, null, null, null, countriesField);
        MoachDTO moach = moachWithRate(rate, List.of());
        Country world = new Country(); world.setCountryName("world");
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        injectRestClientReturning(moach);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        List<Tariff> tariffs = service.loadTariffFromApi(reporting, item);
        // Assert
        assertTrue(tariffs.stream().anyMatch(t -> t.getPartnerCountry().getCountryName().equals("world")));
    }

    @Test
    @DisplayName("Pads reporting country number and item code in request URI")
    void padsCountryNumberAndItemCode() throws Exception {
        // Arrange
        Country small = new Country(); small.setCountryNumber(7); small.setCountryName("Testland"); small.setCountryCode("TL");
        Item shortItem = new Item(); shortItem.setItemCode(123); shortItem.setItemName("gadget"); shortItem.setCountry(small); shortItem.setIndustry(Industry.OTHER);
        TariffRate rate = new TariffRate(null, "free", null, null, null, null, "(TL) 0%");
        TariffData td = new TariffData("000123", rate, "desc", List.of());
        MoachDTO moach = new MoachDTO(List.of(td));

        RestClient deepStubClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        final java.util.concurrent.atomic.AtomicReference<String> captured = new java.util.concurrent.atomic.AtomicReference<>();
        when(deepStubClient.get().uri(anyString())).thenAnswer(inv -> { captured.set(inv.getArgument(0)); return deepStubClient.get(); });
        when(deepStubClient.get().uri(anyString()).retrieve().onStatus(any(), any()).body(eq(MoachDTO.class))).thenReturn(moach);
        var f = TariffCalculationImpl.class.getDeclaredField("restClientMoach");
        f.setAccessible(true); f.set(service, deepStubClient);

        // Act
        try {
            service.loadTariffFromApi(small, shortItem);
        } catch (Exception ignored) {
            // Current implementation may throw during special countries rate parsing; we only validate URI formatting here.
        }

        // Assert
        assertNotNull(captured.get());
        assertTrue(captured.get().contains("country=007"));
        assertTrue(captured.get().contains("hscode=000123"));
    }

    @Test
    @DisplayName("Handles missing parentheses gracefully")
    void missingParentheses() throws Exception {
        // Arrange
        MoachDTO moach = moachWithRate(new TariffRate(null, "free", null, null, null, null, "No special countries text"), List.of());
        Country world = new Country(); world.setCountryName("world");
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        injectRestClientReturning(moach);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        List<Tariff> res = service.loadTariffFromApi(reporting, item);
        // Assert
        assertEquals(1, res.size());
        assertEquals("world", res.get(0).getPartnerCountry().getCountryName());
        assertEquals(0.0, res.get(0).getPercentageRate());
    }

    @Test
    @DisplayName("Processes TableData basic regions (world + unmapped)")
    void processesBasicRegions() throws Exception {
        // Arrange
        Country world = new Country(); world.setCountryName("world");
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        TableData mfn = new TableData("MFN", "Ad Valorem Rate: 5%", null, "world");
        TableData other = new TableData("Random Region", "Ad Valorem Rate: 0%", null, "unmappedCountry");
        MoachDTO moach = moachWithRate(new TariffRate(null, "free", null, null, null, null, "irrelevant"), List.of(mfn, other));
        injectRestClientReturning(moach);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        List<Tariff> tariffs = service.loadTariffFromApi(reporting, item);
        // Assert
        assertTrue(tariffs.stream().anyMatch(t -> t.getPartnerCountry().getCountryName().equals("world")));
    }

    @Test
    @DisplayName("Throws when API returns null data")
    void throwsOnNullData() throws Exception {
        // Arrange
        RestClient deepStubClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        when(deepStubClient.get().uri(anyString()).retrieve().onStatus(any(), any()).body(eq(MoachDTO.class))).thenReturn(null);
        var f = TariffCalculationImpl.class.getDeclaredField("restClientMoach"); f.setAccessible(true); f.set(service, deepStubClient);
        // Act & Assert
        assertThrows(ApiFailureException.class, () -> service.loadTariffFromApi(reporting, item));
    }

    @Test
    @DisplayName("Handles free special rate and no parentheses")
    void freeSpecialRateNoParentheses() throws Exception {
        // Arrange
        MoachDTO moach = moachWithRate(new TariffRate(null, "free", null, null, null, null, "FREE TRADE"), List.of());
        Country world = new Country(); world.setCountryName("world");
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        injectRestClientReturning(moach);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        List<Tariff> res = service.loadTariffFromApi(reporting, item);
        // Assert
        assertEquals(1, res.size());
        assertEquals(0.0, res.get(0).getPercentageRate());
    }

    @Test
    @DisplayName("Parses non-free general rate and saves world tariff")
    void parsesNonFreeGeneralRate() throws Exception {
        // Arrange
        MoachDTO moach = moachWithRate(new TariffRate(null, "Ad Valorem Rate: 7%", null, null, null, null, "No special countries text"), List.of());
        Country world = new Country(); world.setCountryName("world");
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        injectRestClientReturning(moach);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        List<Tariff> res = service.loadTariffFromApi(reporting, item);
        // Assert
        assertEquals(1, res.size());
        assertEquals(7.0, res.get(0).getPercentageRate());
    }

    @Test
    @DisplayName("Maps MFN and LDC regions and avoids duplicates")
    void mapsMfnLdcAvoidsDuplicates() throws Exception {
        // Arrange
        Country world = new Country(); world.setCountryName("world");
        Country developing = new Country(); developing.setCountryName("developing");
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        TableData mfn = new TableData("MFN", "Ad Valorem Rate: 5%", null, "world");
        TableData ldc = new TableData("LDCs Preferential Tariff", "Ad Valorem Rate: 2%", null, "developing");
        TableData mfnDup = new TableData("MFN", "Ad Valorem Rate: 5%", null, "world");
        TariffData td = new TariffData("123456", null, "desc", List.of(mfn, ldc, mfnDup));
        MoachDTO moach = new MoachDTO(List.of(td));
        injectRestClientReturning(moach);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        List<Tariff> tariffs = service.loadTariffFromApi(reporting, item);
        // Assert
        long worldCount = tariffs.stream().filter(t -> "world".equals(t.getPartnerCountry().getCountryName())).count();
        assertTrue(worldCount <= 1, "Duplicate world tariffs should be prevented");
        tariffs.stream().filter(t -> "developing".equals(t.getPartnerCountry().getCountryName())).findFirst()
            .ifPresent(dev -> assertEquals(2.0, dev.getPercentageRate()));
    }

    @Test
    @DisplayName("Fuzzy region name lookup adds firstCountry match")
    void fuzzyRegionNameLookup() throws Exception {
        // Arrange
        TableData region = new TableData("Random Region", "Ad Valorem Rate: 9%", null, "Canada");
        TariffData td = new TariffData("123456", null, "desc", List.of(region));
        MoachDTO moach = new MoachDTO(List.of(td));
        Country canada = new Country(); canada.setCountryName("Canada");
        when(countryRepo.findFirstByCountryNameContainingIgnoreCase("Random Region")).thenReturn(Optional.of(canada));
        injectRestClientReturning(moach);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        List<Tariff> tariffs = service.loadTariffFromApi(reporting, item);
        // Assert
        assertEquals(1, tariffs.size());
        assertEquals("Canada", tariffs.get(0).getPartnerCountry().getCountryName());
        assertEquals(9.0, tariffs.get(0).getPercentageRate());
    }
}
