package com.tariff.calculation.tariffCalc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import com.tariff.calculation.tariffCalc.category.Industry;
import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.SelectedItemsDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffDetailsforItemDTO;
import com.tariff.calculation.tariffCalc.dto.bankServiceDto.TariffItemFilterDTO;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffDetails;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;

/**
 * Comprehensive unit tests for BankIndustrySearchServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BankIndustrySearchServiceImpl Unit Tests")
class BankIndustrySearchServiceImplTest {

    @Mock
    private ItemRepo itemRepo;
    
    @Mock
    private TariffRepo tariffRepo;
    
    @Mock
    private CountryRepo countryRepo;

    private BankIndustrySearchServiceImpl bankIndustrySearchService;

    // Test data
    private Country usaCountry;
    private Country chinaCountry;
    private Country canadaCountry;
    private Item electronicsItem;
    private Item smartphoneItem;
    private Tariff tariff1;
    private Tariff tariff2;
    private Tariff tariff3;

    @BeforeEach
    void setUp() {
        bankIndustrySearchService = new BankIndustrySearchServiceImpl(itemRepo, tariffRepo, countryRepo);
        
        // Setup test data
        usaCountry = new Country();
        usaCountry.setCountryNumber(1);
        usaCountry.setCountryName("United States");
        usaCountry.setCountryCode("USA");
        usaCountry.setIsDeveloping(false);

        chinaCountry = new Country();
        chinaCountry.setCountryNumber(2);
        chinaCountry.setCountryName("China");
        chinaCountry.setCountryCode("CHN");
        chinaCountry.setIsDeveloping(true);

        canadaCountry = new Country();
        canadaCountry.setCountryNumber(3);
        canadaCountry.setCountryName("Canada");
        canadaCountry.setCountryCode("CAN");
        canadaCountry.setIsDeveloping(false);

        electronicsItem = new Item();
        electronicsItem.setId(1);
        electronicsItem.setItemCode(1001);
        electronicsItem.setItemName("Electronics");
        electronicsItem.setIndustry(Industry.TECHNOLOGY);
        electronicsItem.setCountry(usaCountry);

        smartphoneItem = new Item();
        smartphoneItem.setId(2);
        smartphoneItem.setItemCode(1002);
        smartphoneItem.setItemName("Smartphone");
        smartphoneItem.setIndustry(Industry.TECHNOLOGY);
        smartphoneItem.setCountry(usaCountry);

        tariff1 = new Tariff();
        tariff1.setId(1);
        tariff1.setReportingCountry(usaCountry);
        tariff1.setPartnerCountry(chinaCountry);
        tariff1.setItem(electronicsItem);
        tariff1.setPercentageRate(15.0);
        tariff1.setLocalDate(LocalDate.of(2023, 6, 15));
        tariff1.setDescription("Technology tariff");

        tariff2 = new Tariff();
        tariff2.setId(2);
        tariff2.setReportingCountry(usaCountry);
        tariff2.setPartnerCountry(canadaCountry);
        tariff2.setItem(electronicsItem);
        tariff2.setPercentageRate(5.0);
        tariff2.setLocalDate(LocalDate.of(2023, 8, 20));
        tariff2.setDescription("NAFTA tariff");

        tariff3 = new Tariff();
        tariff3.setId(3);
        tariff3.setReportingCountry(usaCountry);
        tariff3.setPartnerCountry(chinaCountry);
        tariff3.setItem(smartphoneItem);
        tariff3.setPercentageRate(25.0);
        tariff3.setLocalDate(LocalDate.of(2023, 9, 10)); // Changed to 2023 to be in range
        tariff3.setDescription("Smartphone tariff");
    }

    @Nested
    @DisplayName("getAllItemsAvailableInTheIndustry() Tests")
    class GetAllItemsAvailableInTheIndustryTests {

        @Test
        @DisplayName("Should return list of item names when items exist in industry and date range")
        void shouldReturnItemNamesWhenItemsExistInIndustryAndDateRange() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            List<Item> mockItems = Arrays.asList(electronicsItem, smartphoneItem);
            List<Tariff> electronicsTraiffs = Arrays.asList(tariff1, tariff2);
            List<Tariff> smartphoneTariffs = Arrays.asList(tariff3);
            
            when(itemRepo.findByIndustry(Industry.TECHNOLOGY)).thenReturn(mockItems);
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(electronicsTraiffs);
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, smartphoneItem)).thenReturn(smartphoneTariffs);

            // Act
            List<String> result = bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(2, result.size(), "Should return 2 items");
            assertTrue(result.contains("Electronics"), "Should contain Electronics");
            assertTrue(result.contains("Smartphone"), "Should contain Smartphone");
            
            // Verify repository interactions
            verify(itemRepo, times(1)).findByIndustry(Industry.TECHNOLOGY);
            verify(countryRepo, times(2)).findByCountryName("United States"); // Called once per item
            verify(tariffRepo, times(1)).findByReportingCountryAndItem(usaCountry, electronicsItem);
            verify(tariffRepo, times(1)).findByReportingCountryAndItem(usaCountry, smartphoneItem);
        }

        @Test
        @DisplayName("Should return empty list when no items exist in industry")
        void shouldReturnEmptyListWhenNoItemsExistInIndustry() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "AEROSPACE", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            when(itemRepo.findByIndustry(Industry.AEROSPACE)).thenReturn(Collections.emptyList());

            // Act
            List<String> result = bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Should return empty list when no items found");
            
            verify(itemRepo, times(1)).findByIndustry(Industry.AEROSPACE);
            verify(countryRepo, never()).findByCountryName(anyString());
            verify(tariffRepo, never()).findByReportingCountryAndItem(any(), any());
        }

        @Test
        @DisplayName("Should return empty list when items exist but no tariffs in date range")
        void shouldReturnEmptyListWhenItemsExistButNoTariffsInDateRange() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "TECHNOLOGY", 
                "2025-01-01", 
                "2025-12-31"
            );
            
            List<Item> mockItems = Arrays.asList(electronicsItem);
            List<Tariff> tariffs = Arrays.asList(tariff1); // tariff1 is from 2023, outside date range
            
            when(itemRepo.findByIndustry(Industry.TECHNOLOGY)).thenReturn(mockItems);
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(tariffs);

            // Act
            List<String> result = bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Should return empty list when no tariffs in date range");
            
            verify(itemRepo, times(1)).findByIndustry(Industry.TECHNOLOGY);
            verify(countryRepo, times(1)).findByCountryName("United States");
            verify(tariffRepo, times(1)).findByReportingCountryAndItem(usaCountry, electronicsItem);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when country not found")
        void shouldThrowIllegalArgumentExceptionWhenCountryNotFound() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "NonExistentCountry",
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            List<Item> mockItems = Arrays.asList(electronicsItem);
            
            when(itemRepo.findByIndustry(Industry.TECHNOLOGY)).thenReturn(mockItems);
            when(countryRepo.findByCountryName("NonExistentCountry")).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO),
                "Should throw IllegalArgumentException when country not found"
            );
            
            assertEquals("Country not found.", exception.getMessage());
            verify(itemRepo, times(1)).findByIndustry(Industry.TECHNOLOGY);
            verify(countryRepo, times(1)).findByCountryName("NonExistentCountry");
        }

        @Test
        @DisplayName("Should handle items with no tariff records")
        void shouldHandleItemsWithNoTariffRecords() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            List<Item> mockItems = Arrays.asList(electronicsItem);
            
            when(itemRepo.findByIndustry(Industry.TECHNOLOGY)).thenReturn(mockItems);
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(Collections.emptyList());

            // Act
            List<String> result = bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Should return empty list when item has no tariff records");
            
            verify(itemRepo, times(1)).findByIndustry(Industry.TECHNOLOGY);
            verify(countryRepo, times(1)).findByCountryName("United States");
            verify(tariffRepo, times(1)).findByReportingCountryAndItem(usaCountry, electronicsItem);
        }
    }

    @Nested
    @DisplayName("getTariffDetailsForItem() Tests")
    class getTariffDetailsForItemTests {

        @Test
        @DisplayName("Should return tariff details when valid item and data exist")
        void shouldReturnTariffDetailsWhenValidItemAndDataExist() {
            // Arrange
            SelectedItemsDTO selectedItemsDTO = new SelectedItemsDTO(
                "Electronics",
                "United States", 
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            List<Tariff> itemTariffs = Arrays.asList(tariff1, tariff2);
            List<Tariff> chinaTariffs = Arrays.asList(tariff1);
            List<Tariff> canadaTariffs = Arrays.asList(tariff2);
            
            when(itemRepo.findByItemName("Electronics")).thenReturn(Optional.of(electronicsItem));
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(itemTariffs);
            when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(usaCountry, chinaCountry, electronicsItem)).thenReturn(chinaTariffs);
            when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(usaCountry, canadaCountry, electronicsItem)).thenReturn(canadaTariffs);

            // Act
            TariffDetailsforItemDTO result = bankIndustrySearchService.getTariffDetailsForItem(selectedItemsDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(1001, result.hscode(), "HSCode should match item code");
            assertEquals("Electronics", result.itemName(), "Item name should match");
            assertNotNull(result.tariffDetailsList(), "Tariff details should not be null");
            assertFalse(result.tariffDetailsList().isEmpty(), "Tariff details should not be empty");
            
            // Verify at least one country has tariff details
            assertTrue(result.tariffDetailsList().size() <= 10, "Should not exceed top 10 countries");
            
            // Verify repository interactions
            verify(itemRepo, times(1)).findByItemName("Electronics");
            verify(countryRepo, times(1)).findByCountryName("United States");
            verify(tariffRepo, times(1)).findByReportingCountryAndItem(usaCountry, electronicsItem);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when item not found")
        void shouldThrowIllegalArgumentExceptionWhenItemNotFound() {
            // Arrange
            SelectedItemsDTO selectedItemsDTO = new SelectedItemsDTO(
                "NonExistentItem",
                "United States", 
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            when(itemRepo.findByItemName("NonExistentItem")).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankIndustrySearchService.getTariffDetailsForItem(selectedItemsDTO),
                "Should throw IllegalArgumentException when item not found"
            );
            
            assertEquals("Item not found.", exception.getMessage());
            verify(itemRepo, times(1)).findByItemName("NonExistentItem");
            verify(countryRepo, never()).findByCountryName(anyString());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when reporting country not found")
        void shouldThrowIllegalArgumentExceptionWhenReportingCountryNotFound() {
            // Arrange
            SelectedItemsDTO selectedItemsDTO = new SelectedItemsDTO(
                "Electronics",
                "NonExistentCountry", 
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            when(itemRepo.findByItemName("Electronics")).thenReturn(Optional.of(electronicsItem));
            when(countryRepo.findByCountryName("NonExistentCountry")).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankIndustrySearchService.getTariffDetailsForItem(selectedItemsDTO),
                "Should throw IllegalArgumentException when country not found"
            );
            
            assertEquals("Country not found.", exception.getMessage());
            verify(itemRepo, times(1)).findByItemName("Electronics");
            verify(countryRepo, times(1)).findByCountryName("NonExistentCountry");
        }

        @Test
        @DisplayName("Should return empty tariff details when no partner countries exist")
        void shouldReturnEmptyTariffDetailsWhenNoPartnerCountriesExist() {
            // Arrange
            SelectedItemsDTO selectedItemsDTO = new SelectedItemsDTO(
                "Electronics",
                "United States", 
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            when(itemRepo.findByItemName("Electronics")).thenReturn(Optional.of(electronicsItem));
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(Collections.emptyList());

            // Act
            TariffDetailsforItemDTO result = bankIndustrySearchService.getTariffDetailsForItem(selectedItemsDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertEquals(1001, result.hscode(), "HSCode should match item code");
            assertEquals("Electronics", result.itemName(), "Item name should match");
            assertNotNull(result.tariffDetailsList(), "Tariff details should not be null");
            assertTrue(result.tariffDetailsList().isEmpty(), "Tariff details should be empty when no partner countries");
            
            verify(itemRepo, times(1)).findByItemName("Electronics");
            verify(countryRepo, times(1)).findByCountryName("United States");
            verify(tariffRepo, times(1)).findByReportingCountryAndItem(usaCountry, electronicsItem);
        }

        @Test
        @DisplayName("Should filter out tariffs with zero percentage rate")
        void shouldFilterOutTariffsWithZeroPercentageRate() {
            // Arrange
            SelectedItemsDTO selectedItemsDTO = new SelectedItemsDTO(
                "Electronics",
                "United States", 
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            Tariff zeroTariff = new Tariff();
            zeroTariff.setId(4);
            zeroTariff.setReportingCountry(usaCountry);
            zeroTariff.setPartnerCountry(chinaCountry);
            zeroTariff.setItem(electronicsItem);
            zeroTariff.setPercentageRate(0.0); // Zero rate - should be filtered out
            zeroTariff.setLocalDate(LocalDate.of(2023, 6, 15));
            
            List<Tariff> itemTariffs = Arrays.asList(tariff1);
            List<Tariff> chinaTariffsWithZero = Arrays.asList(tariff1, zeroTariff);
            
            when(itemRepo.findByItemName("Electronics")).thenReturn(Optional.of(electronicsItem));
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(itemTariffs);
            when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(usaCountry, chinaCountry, electronicsItem)).thenReturn(chinaTariffsWithZero);

            // Act
            TariffDetailsforItemDTO result = bankIndustrySearchService.getTariffDetailsForItem(selectedItemsDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertNotNull(result.tariffDetailsList(), "Tariff details should not be null");
            
            // Verify zero-rate tariffs are filtered out
            if (!result.tariffDetailsList().isEmpty()) {
                for (TariffDetails details : result.tariffDetailsList()) {
                    for (Tariff tariff : details.getTariffList()) {
                        assertTrue(tariff.getPercentageRate() > 0.0, "All tariffs should have positive rates");
                    }
                }
            }
        }

        @Test
        @DisplayName("Should filter tariffs by date range correctly")
        void shouldFilterTariffsByDateRangeCorrectly() {
            // Arrange
            SelectedItemsDTO selectedItemsDTO = new SelectedItemsDTO(
                "Electronics",
                "United States", 
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-07-31" // Only includes tariff1 (June 2023), excludes tariff2 (August 2023)
            );
            
            List<Tariff> itemTariffs = Arrays.asList(tariff1); // Only tariff1 is in range
            List<Tariff> chinaTariffs = Arrays.asList(tariff1);
            
            when(itemRepo.findByItemName("Electronics")).thenReturn(Optional.of(electronicsItem));
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(itemTariffs);
            when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(usaCountry, chinaCountry, electronicsItem)).thenReturn(chinaTariffs);

            // Act
            TariffDetailsforItemDTO result = bankIndustrySearchService.getTariffDetailsForItem(selectedItemsDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertNotNull(result.tariffDetailsList(), "Tariff details should not be null");
            
            // Verify date filtering - only tariffs within 2023-01-01 to 2023-07-31 should be included
            for (TariffDetails details : result.tariffDetailsList()) {
                for (Tariff tariff : details.getTariffList()) {
                    assertTrue(tariff.getLocalDate().getYear() == 2023, "Year should be 2023");
                    assertTrue(tariff.getLocalDate().getMonthValue() <= 7, "Month should be <= 7");
                }
            }
        }

        @Test
        @DisplayName("Should handle null dates in tariffs gracefully")
        void shouldHandleNullDatesInTariffsGracefully() {
            // Arrange
            SelectedItemsDTO selectedItemsDTO = new SelectedItemsDTO(
                "Electronics",
                "United States", 
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            Tariff nullDateTariff = new Tariff();
            nullDateTariff.setId(5);
            nullDateTariff.setReportingCountry(usaCountry);
            nullDateTariff.setPartnerCountry(chinaCountry);
            nullDateTariff.setItem(electronicsItem);
            nullDateTariff.setPercentageRate(10.0);
            nullDateTariff.setLocalDate(null); // Null date - should be filtered out
            
            List<Tariff> itemTariffs = Arrays.asList(tariff1);
            List<Tariff> chinaTariffsWithNull = Arrays.asList(tariff1, nullDateTariff);
            
            when(itemRepo.findByItemName("Electronics")).thenReturn(Optional.of(electronicsItem));
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            when(tariffRepo.findByReportingCountryAndItem(usaCountry, electronicsItem)).thenReturn(itemTariffs);
            when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(usaCountry, chinaCountry, electronicsItem)).thenReturn(chinaTariffsWithNull);

            // Act & Assert
            assertDoesNotThrow(() -> {
                TariffDetailsforItemDTO result = bankIndustrySearchService.getTariffDetailsForItem(selectedItemsDTO);
                assertNotNull(result, "Result should not be null");
                
                // Verify null date tariffs are filtered out
                for (TariffDetails details : result.tariffDetailsList()) {
                    for (Tariff tariff : details.getTariffList()) {
                        assertNotNull(tariff.getLocalDate(), "All included tariffs should have non-null dates");
                    }
                }
            }, "Should handle null dates without throwing exceptions");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create service instance with all required dependencies")
        void shouldCreateServiceInstanceWithAllRequiredDependencies() {
            // Arrange & Act
            BankIndustrySearchServiceImpl service = new BankIndustrySearchServiceImpl(
                itemRepo, 
                tariffRepo, 
                countryRepo
            );

            // Assert
            assertNotNull(service, "Service instance should not be null");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle empty date strings gracefully")
        void shouldHandleEmptyDateStringsGracefully() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "TECHNOLOGY", 
                "", // Empty start date
                "" // Empty end date
            );

            // Act & Assert
            assertThrows(
                Exception.class,
                () -> bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO),
                "Should throw exception for empty date strings"
            );
        }

        @Test
        @DisplayName("Should handle invalid date format gracefully")
        void shouldHandleInvalidDateFormatGracefully() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "TECHNOLOGY", 
                "invalid-date", 
                "2023-12-31"
            );

            // Act & Assert
            assertThrows(
                Exception.class,
                () -> bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO),
                "Should throw exception for invalid date format"
            );
        }

        @Test
        @DisplayName("Should handle null industry enum gracefully")
        void shouldHandleNullIndustryEnumGracefully() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "INVALID_INDUSTRY", 
                "2023-01-01", 
                "2023-12-31"
            );

            // Act & Assert
            assertThrows(
                IllegalArgumentException.class,
                () -> bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO),
                "Should throw IllegalArgumentException for invalid industry"
            );
        }

        @Test
        @DisplayName("Should handle large number of items efficiently")
        void shouldHandleLargeNumberOfItemsEfficiently() {
            // Arrange
            TariffItemFilterDTO filterDTO = new TariffItemFilterDTO(
                "United States",
                "TECHNOLOGY", 
                "2023-01-01", 
                "2023-12-31"
            );
            
            // Create 100 mock items
            List<Item> largeItemList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Item item = new Item();
                item.setId(i);
                item.setItemName("Item" + i);
                item.setItemCode(1000 + i);
                item.setIndustry(Industry.TECHNOLOGY);
                largeItemList.add(item);
            }
            
            when(itemRepo.findByIndustry(Industry.TECHNOLOGY)).thenReturn(largeItemList);
            when(countryRepo.findByCountryName("United States")).thenReturn(Optional.of(usaCountry));
            
            // Mock empty tariff lists for all items to avoid complex setup
            for (Item item : largeItemList) {
                when(tariffRepo.findByReportingCountryAndItem(usaCountry, item)).thenReturn(Collections.emptyList());
            }

            // Act
            List<String> result = bankIndustrySearchService.getAllItemsAvailableInTheIndustry(filterDTO);

            // Assert
            assertNotNull(result, "Result should not be null");
            assertTrue(result.isEmpty(), "Should return empty list when no tariffs exist");
            
            // Verify all items were processed
            verify(itemRepo, times(1)).findByIndustry(Industry.TECHNOLOGY);
            verify(tariffRepo, times(100)).findByReportingCountryAndItem(eq(usaCountry), any(Item.class));
        }
    }
}