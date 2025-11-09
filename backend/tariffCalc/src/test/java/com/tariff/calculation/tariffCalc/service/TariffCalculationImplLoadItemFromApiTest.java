package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

/**
 * Unit tests for loadItemFromApi method in TariffCalculationImpl, verifying industry inference via
 * embeddings, world-country fallback to OTHER category, and persistence behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TariffCalculationImpl loadItemFromApi Tests")
class TariffCalculationImplLoadItemFromApiTest {

    @Mock com.tariff.calculation.tariffCalc.country.CountryRepo countryRepo;
    @Mock ItemRepo itemRepo;
    @Mock TariffRepo tariffRepo;
    @Mock RestClient.Builder restClientBuilder;
    @Mock RestClient restClient;
    @Mock EmbeddingService embeddingService;
    @Mock com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    TariffCalculationImpl service;

    @BeforeEach
    void init() {
        when(restClientBuilder.clone()).thenReturn(restClientBuilder);
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        service = new TariffCalculationImpl(countryRepo, itemRepo, tariffRepo, restClientBuilder, objectMapper, embeddingService);
    }

    @Test
    @DisplayName("Non-world endpoint maps category to Technology")
    void loadItemFromApi_NonWorldCountry_MapsIndustry() throws com.tariff.calculation.tariffCalc.exception.ApiFailureException {
        // Arrange
        Country reporting = new Country();
        reporting.setCountryNumber(840);
        reporting.setCountryName("USA");
        String itemName = "High-Tech Device";
        var mockDto = new com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO(
            new com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemData(
                List.of(new com.tariff.calculation.tariffCalc.dto.itemApiDto.SixDigitCodes(
                    null, null, null, null, "123456", "Advanced electronics", null))));
        RestClient deepStubClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        when(deepStubClient.get().uri(anyString()).retrieve().onStatus(any(), any()).body(eq(com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO.class)))
            .thenReturn(mockDto);
        TariffCalculationImpl spyService = spy(service);
        try {
            var f = TariffCalculationImpl.class.getDeclaredField("restClientMoach"); f.setAccessible(true); f.set(spyService, deepStubClient);
        } catch (Exception e) { fail("Failed to inject mock RestClient: " + e.getMessage()); }
        com.tariff.calculation.tariffCalc.category.Category fakeCategory = new com.tariff.calculation.tariffCalc.category.Category();
        fakeCategory.setName("Technology");
        when(embeddingService.getEmbeddings(anyString(), anyString())).thenReturn(fakeCategory);
        when(itemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Item item = spyService.loadItemFromApi(itemName, reporting);
        // Assert
        assertNotNull(item);
        assertEquals(reporting, item.getCountry());
        assertEquals(Industry.TECHNOLOGY, item.getIndustry());
    }

    @Test
    @DisplayName("World country falls back to OTHER when embedding fails")
    void loadItemFromApi_WorldCountry_FallbackIndustry() throws com.tariff.calculation.tariffCalc.exception.ApiFailureException {
        // Arrange
        Country world = new Country(); world.setCountryNumber(-2); world.setCountryName("world");
        var mockDto = new com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO(
            new com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemData(
                List.of(new com.tariff.calculation.tariffCalc.dto.itemApiDto.SixDigitCodes(
                    null, null, null, null, "654321", "Generic item", null))));
        RestClient deepStubClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
        when(deepStubClient.get().uri(anyString()).retrieve().onStatus(any(), any()).body(eq(com.tariff.calculation.tariffCalc.dto.itemApiDto.ItemRetrievalDTO.class)))
            .thenReturn(mockDto);
        TariffCalculationImpl spyService = spy(service);
        try {
            var f = TariffCalculationImpl.class.getDeclaredField("restClientMoach"); f.setAccessible(true); f.set(spyService, deepStubClient);
        } catch (Exception e) { fail("Reflection injection failed: " + e.getMessage()); }
        when(embeddingService.getEmbeddings(anyString(), anyString())).thenThrow(new RuntimeException("embed fail"));
        when(itemRepo.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        Item item = spyService.loadItemFromApi("Generic item", world);
        // Assert
        assertEquals(Industry.OTHER, item.getIndustry());
    }
}
