package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.country.CountryRepo;
import com.tariff.calculation.tariffCalc.dto.TariffDeleteDTO;
import com.tariff.calculation.tariffCalc.item.Item;
import com.tariff.calculation.tariffCalc.item.ItemRepo;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.category.Industry;

/**
 * Comprehensive unit tests for CrudServiceImpl
 * Tests all CRUD operations, business logic, and exception handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrudService Unit Tests")
class CrudServiceImplTest {

    @Mock
    private TariffRepo tariffRepo;

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private CountryRepo countryRepo;

    @InjectMocks
    private CrudServiceImpl crudService;

    private Country testReportingCountry;
    private Country testPartnerCountry;
    private Item testItem;
    private Tariff testTariff1;
    private Tariff testTariff2;
    private TariffDeleteDTO testTariffDeleteDTO;

    @BeforeEach
    void setUp() {
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

        testItem = new Item();
        testItem.setId(1);
        testItem.setItemCode(123456);
        testItem.setItemName("electronic840");
        testItem.setCountry(testReportingCountry);
        testItem.setIndustry(Industry.TECHNOLOGY);

        testTariff1 = new Tariff();
        testTariff1.setId(1);
        testTariff1.setReportingCountry(testReportingCountry);
        testTariff1.setPartnerCountry(testPartnerCountry);
        testTariff1.setItem(testItem);
        testTariff1.setPercentageRate(15.5);
        testTariff1.setDescription("Electronics tariff");

        testTariff2 = new Tariff();
        testTariff2.setId(2);
        testTariff2.setReportingCountry(testReportingCountry);
        testTariff2.setPartnerCountry(testPartnerCountry);
        testTariff2.setItem(testItem);
        testTariff2.setPercentageRate(10.0);
        testTariff2.setDescription("Alternative electronics tariff");

        testTariffDeleteDTO = new TariffDeleteDTO("USA", "China", "Electronics");
    }

    // ===== DELETE TARIFF TESTS =====

    @Test
    @DisplayName("Should delete tariffs when valid countries and items exist")
    void deleteTariff_ShouldDeleteTariffs_WhenValidCountriesAndItemsExist() {
        // Arrange
        List<Tariff> tariffsToDelete = Arrays.asList(testTariff1, testTariff2);
        
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemName("electronic840")).thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, testItem))
                .thenReturn(tariffsToDelete);

        // Act
        crudService.deleteTariff(testTariffDeleteDTO);

        // Assert
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
        verify(itemRepo, times(1)).findByItemName("electronic840");
        verify(tariffRepo, times(1)).findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, testItem);
        verify(tariffRepo, times(1)).delete(testTariff1);
        verify(tariffRepo, times(1)).delete(testTariff2);
    }

    @Test
    @DisplayName("Should delete tariffs using fallback item name when primary lookup fails")
    void deleteTariff_ShouldDeleteTariffsUsingFallback_WhenPrimaryItemLookupFails() {
        // Arrange
        Item fallbackItem = new Item();
        fallbackItem.setId(2);
        fallbackItem.setItemName("electronicgeneral");
        List<Tariff> tariffsToDelete = Arrays.asList(testTariff1);
        
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemName("electronic840")).thenReturn(Optional.empty());
        when(itemRepo.findByItemName("electronicgeneral")).thenReturn(Optional.of(fallbackItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, fallbackItem))
                .thenReturn(tariffsToDelete);

        // Act
        crudService.deleteTariff(testTariffDeleteDTO);

        // Assert
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
        verify(itemRepo, times(1)).findByItemName("electronic840");
        verify(itemRepo, times(1)).findByItemName("electronicgeneral");
        verify(tariffRepo, times(1)).findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, fallbackItem);
        verify(tariffRepo, times(1)).delete(testTariff1);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when reporting country not found")
    void deleteTariff_ShouldThrowException_WhenReportingCountryNotFound() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteTariff(testTariffDeleteDTO));
        
        assertEquals("Reporting country not found", exception.getMessage());
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, never()).findByCountryName("China");
        verify(itemRepo, never()).findByItemName(anyString());
        verify(tariffRepo, never()).findByReportingCountryAndPartnerCountryAndItem(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when partner country not found")
    void deleteTariff_ShouldThrowException_WhenPartnerCountryNotFound() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteTariff(testTariffDeleteDTO));
        
        assertEquals("Partner country not found", exception.getMessage());
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
        verify(itemRepo, never()).findByItemName(anyString());
        verify(tariffRepo, never()).findByReportingCountryAndPartnerCountryAndItem(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when item not found")
    void deleteTariff_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemName("electronic840")).thenReturn(Optional.empty());
        when(itemRepo.findByItemName("electronicgeneral")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteTariff(testTariffDeleteDTO));
        
        assertEquals("Item not found for item Electronics", exception.getMessage());
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
        verify(itemRepo, times(1)).findByItemName("electronic840");
        verify(itemRepo, times(1)).findByItemName("electronicgeneral");
        verify(tariffRepo, never()).findByReportingCountryAndPartnerCountryAndItem(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no tariffs found to delete")
    void deleteTariff_ShouldThrowException_WhenNoTariffsFoundToDelete() {
        // Arrange
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemName("electronic840")).thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, testItem))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteTariff(testTariffDeleteDTO));
        
        assertEquals("This combination doesnt exists", exception.getMessage());
        verify(countryRepo, times(1)).findByCountryName("USA");
        verify(countryRepo, times(1)).findByCountryName("China");
        verify(itemRepo, times(1)).findByItemName("electronic840");
        verify(tariffRepo, times(1)).findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, testItem);
        verify(tariffRepo, never()).delete(any(Tariff.class));
    }

    // ===== DELETE ITEM TESTS =====

    @Test
    @DisplayName("Should delete item when item exists")
    void deleteItem_ShouldDeleteItem_WhenItemExists() {
        // Arrange
        Integer itemId = 1;
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(testItem));

        // Act
        crudService.deleteItem(itemId);

        // Assert
        verify(itemRepo, times(1)).findById(itemId);
        verify(itemRepo, times(1)).delete(testItem);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when item does not exist")
    void deleteItem_ShouldThrowException_WhenItemDoesNotExist() {
        // Arrange
        Integer nonExistentId = 999;
        when(itemRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteItem(nonExistentId));
        
        assertEquals("Item id is incorrect", exception.getMessage());
        verify(itemRepo, times(1)).findById(nonExistentId);
        verify(itemRepo, never()).delete(any(Item.class));
    }

    @Test
    @DisplayName("Should handle negative item ID")
    void deleteItem_ShouldHandleNegativeId() {
        // Arrange
        Integer negativeId = -1;
        when(itemRepo.findById(negativeId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteItem(negativeId));
        
        assertEquals("Item id is incorrect", exception.getMessage());
        verify(itemRepo, times(1)).findById(negativeId);
        verify(itemRepo, never()).delete(any(Item.class));
    }

    // ===== DELETE COUNTRY TESTS =====

    @Test
    @DisplayName("Should delete country when country exists")
    void deleteCountry_ShouldDeleteCountry_WhenCountryExists() {
        // Arrange
        Integer countryId = 840;
        when(countryRepo.findById(countryId)).thenReturn(Optional.of(testReportingCountry));

        // Act
        crudService.deleteCountry(countryId);

        // Assert
        verify(countryRepo, times(1)).findById(countryId);
        verify(countryRepo, times(1)).delete(testReportingCountry);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when country does not exist")
    void deleteCountry_ShouldThrowException_WhenCountryDoesNotExist() {
        // Arrange
        Integer nonExistentId = 999;
        when(countryRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteCountry(nonExistentId));
        
        assertEquals("Country id is incorrect", exception.getMessage());
        verify(countryRepo, times(1)).findById(nonExistentId);
        verify(countryRepo, never()).delete(any(Country.class));
    }

    @Test
    @DisplayName("Should handle negative country ID")
    void deleteCountry_ShouldHandleNegativeId() {
        // Arrange
        Integer negativeId = -1;
        when(countryRepo.findById(negativeId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> crudService.deleteCountry(negativeId));
        
        assertEquals("Country id is incorrect", exception.getMessage());
        verify(countryRepo, times(1)).findById(negativeId);
        verify(countryRepo, never()).delete(any(Country.class));
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle whitespace and case variations in tariff delete DTO")
    void deleteTariff_ShouldHandleWhitespaceAndCase() {
        // Arrange
        TariffDeleteDTO dtoWithWhitespace = new TariffDeleteDTO("  USA  ", " China ", " Electronics ");
        
        when(countryRepo.findByCountryName("  USA  ")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName(" China ")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemName("electronic840")).thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, testItem))
                .thenReturn(Arrays.asList(testTariff1));

        // Act
        crudService.deleteTariff(dtoWithWhitespace);

        // Assert
        verify(countryRepo, times(1)).findByCountryName("  USA  ");
        verify(countryRepo, times(1)).findByCountryName(" China ");
        verify(itemRepo, times(1)).findByItemName("electronic840");
        verify(tariffRepo, times(1)).delete(testTariff1);
    }

    @Test
    @DisplayName("Should handle special characters in item name processing")
    void deleteTariff_ShouldHandleSpecialCharacters() {
        // Arrange
        TariffDeleteDTO specialCharDTO = new TariffDeleteDTO("USA", "China", "Electronics, High-Tech");
        
        when(countryRepo.findByCountryName("USA")).thenReturn(Optional.of(testReportingCountry));
        when(countryRepo.findByCountryName("China")).thenReturn(Optional.of(testPartnerCountry));
        when(itemRepo.findByItemName("electronics, high-tech840")).thenReturn(Optional.of(testItem));
        when(tariffRepo.findByReportingCountryAndPartnerCountryAndItem(
                testReportingCountry, testPartnerCountry, testItem))
                .thenReturn(Arrays.asList(testTariff1));

        // Act
        crudService.deleteTariff(specialCharDTO);

        // Assert
        verify(itemRepo, times(1)).findByItemName("electronics, high-tech840");
        verify(tariffRepo, times(1)).delete(testTariff1);
    }
}