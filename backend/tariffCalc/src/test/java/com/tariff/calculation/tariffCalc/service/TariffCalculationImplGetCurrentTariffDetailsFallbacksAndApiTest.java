package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
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
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

/**
 * Fallback and API-path unit tests for getCurrentTariffDetails method in TariffCalculationImpl.
 * Exercises partner/developing/world tariff precedence, dynamic world tariff creation,
 * nested fallback branches, world item substitution, API loading stubs, and error propagation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TariffCalculation getCurrentTariffDetails Fallbacks & API")
class TariffCalculationImplGetCurrentTariffDetailsFallbacksAndApiTest {

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
    private Country developing;
    private Country world;
    private Item item;

    @BeforeEach
    void init() {
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

        developing = new Country();
        developing.setCountryNumber(-1);
        developing.setCountryName("developing");
        developing.setCountryCode("D");
        developing.setIsDeveloping(true);

        world = new Country();
        world.setCountryNumber(-2);
        world.setCountryName("world");
        world.setCountryCode("W");

        item = new Item();
        item.setId(1);
        item.setItemCode(123456);
        item.setItemName("electronics");
        item.setCountry(reporting);
        item.setIndustry(Industry.TECHNOLOGY);
    }

    @Test
    @DisplayName("Loads from API when cache empty and uses partner tariff")
    void loadsFromApi_UsesPartnerTariff() throws ApiFailureException {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA", "China", "Electronics", 200.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.empty());
        TariffCalculationImpl spyService = spy(service);
        Item loaded = new Item(); loaded.setId(500); loaded.setItemCode(123456); loaded.setItemName("electronics"); loaded.setCountry(reporting); loaded.setIndustry(Industry.TECHNOLOGY);
        doReturn(loaded).when(spyService).loadItemFromApi("electronics", reporting);
        when(tariffRepo.findByReportingCountryAndItem(reporting, loaded)).thenReturn(new java.util.ArrayList<>());
        Tariff partnerTariff = new Tariff(reporting, partner, loaded, 7.5, "Loaded partner tariff", LocalDate.now());
        partnerTariff.setId(321);
        doReturn(List.of(partnerTariff)).when(spyService).loadTariffFromApi(reporting, loaded);
        // Act
        TariffResponseDTO result = spyService.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(7.5, result.tariffRate());
        assertEquals(321, result.tariffId());
        verify(spyService).loadTariffFromApi(reporting, loaded);
    }

    @Test
    @DisplayName("Fallback to developing tariff when partner is developing and partner tariff missing")
    void fallbackToDeveloping_WhenPartnerMissingAndDeveloping() throws ApiFailureException {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA", "China", "Electronics", 100.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Tariff dev = new Tariff(reporting, developing, item, 4.0, "Developing rate", LocalDate.now());
        dev.setId(40);
        Tariff wrld = new Tariff(reporting, world, item, 9.0, "World rate", LocalDate.now());
        wrld.setId(90);
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(dev, wrld));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(developing, reporting, item)).thenReturn(List.of(dev));
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(4.0, res.tariffRate());
        assertEquals(40, res.tariffId());
    }

    @Test
    @DisplayName("Fallback to world when no partner or developing tariff applies")
    void fallbackToWorld_WhenNoPartnerOrDeveloping() throws ApiFailureException {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA", "China", "Electronics", 50.0);
        partner.setIsDeveloping(false);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Tariff wrld = new Tariff(reporting, world, item, 2.5, "World rate", LocalDate.now());
        wrld.setId(250);
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(wrld));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(developing, reporting, item)).thenReturn(List.of());
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(2.5, res.tariffRate());
        assertEquals(250, res.tariffId());
    }

    @Test
    @DisplayName("Creates world fallback tariff when world missing in existing list")
    void createsWorldFallback_WhenWorldMissing() throws ApiFailureException {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA", "China", "Electronics", 120.0);
        partner.setIsDeveloping(false);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Country japan = new Country(); japan.setCountryNumber(392); japan.setCountryName("Japan");
        Tariff random = new Tariff(reporting, japan, item, 11.0, "Japan rate", LocalDate.now());
        random.setId(555);
        var existing = new java.util.ArrayList<>(List.of(random));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(existing);
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(developing, reporting, item)).thenReturn(List.of());
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> { Tariff t = inv.getArgument(0); t.setId(777); return t; });
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(0.0, res.tariffRate());
        assertEquals(777, res.tariffId());
        assertEquals("No trade agreement found", res.tariffDescription());
    }

    @Test
    @DisplayName("World fallback branch creates tariff when none present and partner not developing")
    void worldFallback_CreatesNew() throws ApiFailureException {
        // Arrange
        partner.setIsDeveloping(false);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(new java.util.ArrayList<>());
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(developing, reporting, item)).thenReturn(List.of());
        TariffCalculationImpl spyService = spy(service);
        doReturn(List.of()).when(spyService).loadTariffFromApi(reporting, item);
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> { Tariff t = inv.getArgument(0); t.setId(888); return t; });
        // Act
        TariffResponseDTO res = spyService.getCurrentTariffDetails(new TariffCalculationQueryDTO("USA","China","Electronics", 10.0));
        // Assert
        assertEquals(888, res.tariffId());
        assertEquals(0.0, res.tariffRate());
    }

    @Test
    @DisplayName("Throws when developing country missing in fallback chain")
    void throws_WhenDevelopingMissing() {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA","China","Electronics", 42.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.empty());
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Country france = new Country(); france.setCountryName("France");
        Tariff random = new Tariff(reporting, france, item, 8.0, "Random tariff", LocalDate.now());
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(random));
        // Act & Assert
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> service.getCurrentTariffDetails(dto));
        assertEquals("Developing not found", ex.getMessage());
    }

    @Test
    @DisplayName("Throws when world country missing after developing present but no tariffs")
    void throws_WhenWorldMissing() {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA","China","Electronics", 55.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.empty());
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Country spain = new Country(); spain.setCountryName("Spain");
        Tariff random = new Tariff(reporting, spain, item, 6.0, "Random tariff", LocalDate.now());
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(random));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(developing, reporting, item)).thenReturn(List.of());
        // Act & Assert
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> service.getCurrentTariffDetails(dto));
        assertEquals("World not found", ex.getMessage());
    }

    @Test
    @DisplayName("Creates world fallback when partner is developing but no developing tariff exists")
    void createsWorldFallback_WhenPartnerDeveloping_NoDevelopingTariff() throws ApiFailureException {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA","China","Electronics", 99.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Country brazil = new Country(); brazil.setCountryName("Brazil");
        Tariff random = new Tariff(reporting, brazil, item, 13.0, "Random tariff", LocalDate.now());
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(random));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(developing, reporting, item)).thenReturn(List.of());
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> { Tariff t = inv.getArgument(0); t.setId(909); return t; });
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(0.0, res.tariffRate());
        assertEquals(909, res.tariffId());
        assertEquals("No trade agreement found", res.tariffDescription());
    }

    @Test
    @DisplayName("Developing branch nested world fallback when tariffList missing both")
    void developingBranch_NestedWorldFallback_WhenTariffListMissingBoth() throws ApiFailureException {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA","China","Electronics", 44.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(world));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Tariff devRepoOnly = new Tariff(reporting, developing, item, 7.0, "Repo developing", LocalDate.now());
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(developing, reporting, item)).thenReturn(List.of(devRepoOnly));
        Country norway = new Country(); norway.setCountryName("Norway"); norway.setCountryNumber(578);
        Tariff unrelated = new Tariff(reporting, norway, item, 12.0, "Norway rate", LocalDate.now());
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(unrelated));
        when(tariffRepo.save(any(Tariff.class))).thenAnswer(inv -> { Tariff t = inv.getArgument(0); t.setId(4242); return t; });
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(0.0, res.tariffRate());
        assertEquals(4242, res.tariffId());
        assertEquals("No trade agreement found", res.tariffDescription());
        assertEquals("China", res.partnerCountry());
    }

    @Test
    @DisplayName("Uses world item when reporting not customValid and world item exists")
    void usesWorldItem_WhenReportingNotCustomValid_WorldItemExists() {
        // Arrange
        Country nonCustom = new Country(); nonCustom.setCountryNumber(999); nonCustom.setCountryName("Atlantis"); nonCustom.setCountryCode("AT");
        Country worldCountry = new Country(); worldCountry.setCountryNumber(-2); worldCountry.setCountryName("world"); worldCountry.setCountryCode("W");
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("Atlantis", "China", "Electronics", 500.0);
        Item worldItem = new Item(); worldItem.setId(20); worldItem.setItemCode(543210); worldItem.setItemName("electronics"); worldItem.setCountry(worldCountry); worldItem.setIndustry(Industry.TECHNOLOGY);
        Tariff worldTariff = new Tariff(nonCustom, worldCountry, worldItem, 5.0, "World tariff", LocalDate.now()); worldTariff.setId(99);
        when(countryRepo.findByCountryName("Atlantis")).thenReturn(Optional.of(nonCustom));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(worldCountry));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(itemRepo.findByItemNameAndCountry("electronics", worldCountry)).thenReturn(Optional.of(worldItem));
        when(tariffRepo.findByReportingCountryAndItem(nonCustom, worldItem)).thenReturn(List.of(worldTariff));
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(5.0, res.tariffRate());
        verify(itemRepo).findByItemNameAndCountry("electronics", worldCountry);
        verify(itemRepo, never()).findByItemNameAndCountry("electronics", nonCustom);
    }

    @Test
    @DisplayName("Loads world item from API when world item absent for non customValid reporting country")
    void loadsWorldItemFromApi_WhenWorldItemAbsent() throws ApiFailureException {
        // Arrange
        Country nonCustom = new Country(); nonCustom.setCountryNumber(999); nonCustom.setCountryName("Atlantis"); nonCustom.setCountryCode("AT");
        Country worldCountry = new Country(); worldCountry.setCountryNumber(-2); worldCountry.setCountryName("world"); worldCountry.setCountryCode("W");
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("Atlantis", "China", "Electronics", 250.0);
        when(countryRepo.findByCountryName("Atlantis")).thenReturn(Optional.of(nonCustom));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(worldCountry));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(itemRepo.findByItemNameAndCountry("electronics", worldCountry)).thenReturn(Optional.empty());
        Item loadedItem = new Item(); loadedItem.setId(77); loadedItem.setItemCode(111111); loadedItem.setItemName("electronics"); loadedItem.setCountry(worldCountry); loadedItem.setIndustry(Industry.OTHER);
        TariffCalculationImpl spyService = spy(service);
        doReturn(loadedItem).when(spyService).loadItemFromApi("electronics", worldCountry);
        Tariff worldTariff = new Tariff(nonCustom, worldCountry, loadedItem, 3.5, "World tariff loaded", LocalDate.now()); worldTariff.setId(101);
        when(tariffRepo.findByReportingCountryAndItem(nonCustom, loadedItem)).thenReturn(List.of(worldTariff));
        // Act
        TariffResponseDTO res = spyService.getCurrentTariffDetails(dto);
        // Assert
        assertEquals(3.5, res.tariffRate());
        verify(spyService).loadItemFromApi("electronics", worldCountry);
    }

    @Test
    @DisplayName("Propagates ApiFailureException when loadItemFromApi fails")
    void throws_WhenLoadItemFails() throws ApiFailureException {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA", "China", "Electronics", 300.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.empty());
        TariffCalculationImpl spyService = spy(service);
        doThrow(new ApiFailureException("Item API fail")).when(spyService).loadItemFromApi("electronics", reporting);
        // Act & Assert
        assertThrows(ApiFailureException.class, () -> spyService.getCurrentTariffDetails(dto));
    }

    @Test
    @DisplayName("Use developing tariff when no specific partner tariff found")
    void usesDevelopingTariff_WhenNoSpecificPartnerTariffFound() {
        // Arrange
        TariffCalculationQueryDTO dto = new TariffCalculationQueryDTO("USA", "China", "Electronics", 1000.0);
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(reporting));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(partner));
        when(itemRepo.findByItemNameAndCountry("electronics", reporting)).thenReturn(Optional.of(item));
        Tariff dev = new Tariff(reporting, developing, item, 10.0, "Developing country tariff", LocalDate.now());
        dev.setId(2);
        when(tariffRepo.findByReportingCountryAndItem(reporting, item)).thenReturn(List.of(dev));
        when(countryRepo.findByCountryName("developing")).thenReturn(Optional.of(developing));
        when(countryRepo.findByCountryName("world")).thenReturn(Optional.of(developing));
        // Act
        TariffResponseDTO res = service.getCurrentTariffDetails(dto);
        // Assert
        assertNotNull(res);
        assertEquals(10.0, res.tariffRate());
        assertEquals(2, res.tariffId());
        verify(countryRepo).findByCountryName("developing");
    }
}
