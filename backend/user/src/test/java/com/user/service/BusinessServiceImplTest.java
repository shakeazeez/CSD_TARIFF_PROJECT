package com.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.user.dto.BusinessInfoDTO;
import com.user.dto.BusinessTariffDTO;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BusinessDetails;
import com.user.user.BusinessDetailsRepo;
import com.user.user.BusinessUser;
import com.user.user.User;
import com.user.user.UserRepo;
import com.user.enums.Role;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessUserService Unit Tests")
class BusinessServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private HistoryRepo historyRepo;

    @Mock
    private BusinessDetailsRepo businessDetailsRepo;

    @InjectMocks
    private BusinessServiceImpl businessService;

    private BusinessUser businessUser;
    private User nonBusinessUser;
    private List<History> historyList;

    @BeforeEach
    void setUp() {
        // Create BusinessDetails objects
        BusinessDetails detail1 = new BusinessDetails("Canada", "Electronics");
        BusinessDetails detail2 = new BusinessDetails("Mexico", "Software");
        BusinessDetails detail3 = new BusinessDetails("UK", "Hardware");
        
        businessUser = new BusinessUser(
                "business_user",
                "pw",
                Role.BUSINESS,
                new HashSet<>(Set.of(detail1, detail2, detail3)),
                "USA");

        nonBusinessUser = new User();
        nonBusinessUser.setUsername("regular_user");
        nonBusinessUser.setRole(Role.MEMBER);

        // Create history entries with different counters and dates
        History history1 = new History();
        history1.setTariffId(1);
        history1.setCounter(5);
        history1.setLocalDate(LocalDate.of(2023, 1, 1));
        history1.setUser(businessUser);

        History history2 = new History();
        history2.setTariffId(2);
        history2.setCounter(10);
        history2.setLocalDate(LocalDate.of(2023, 2, 1));
        history2.setUser(businessUser);

        History history3 = new History();
        history3.setTariffId(3);
        history3.setCounter(3);
        history3.setLocalDate(LocalDate.of(2023, 3, 1));
        history3.setUser(businessUser);

        History history4 = new History();
        history4.setTariffId(4);
        history4.setCounter(8);
        history4.setLocalDate(LocalDate.of(2023, 4, 1));
        history4.setUser(businessUser);

        History history5 = new History();
        history5.setTariffId(5);
        history5.setCounter(2);
        history5.setLocalDate(LocalDate.of(2023, 5, 1));
        history5.setUser(businessUser);

        History history6 = new History();
        history6.setTariffId(6);
        history6.setCounter(7);
        history6.setLocalDate(LocalDate.of(2023, 6, 1));
        history6.setUser(businessUser);

        historyList = Arrays.asList(history1, history2, history3, history4, history5, history6);
    }

    @Test
    void getBusinessDetails_Success() {
        // Arrange
        when(userRepo.findByUsername("business_user")).thenReturn(Optional.of(businessUser));
        when(historyRepo.findByUser(businessUser)).thenReturn(historyList);

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("business_user");

        // Assert
        assertNotNull(result);
        assertEquals("USA", result.originCountry());
        assertNotNull(result.tariffs());
        assertEquals(3, result.tariffs().size());
        
        // Check that tariffs contain expected items
        List<String> reportingCountries = result.tariffs().stream()
                .map(BusinessTariffDTO::reportingCountry)
                .toList();
        List<String> items = result.tariffs().stream()
                .map(BusinessTariffDTO::item)
                .toList();
        
        assertTrue(reportingCountries.containsAll(Arrays.asList("Canada", "Mexico", "UK")));
        assertTrue(items.containsAll(Arrays.asList("Electronics", "Software", "Hardware")));
        
        assertNotNull(result.historyTariffIds());
        assertEquals(5, result.historyTariffIds().size()); // Should return top 5

        // Verify the top 5 entries are returned in order of counter (descending)
        Map<Integer, LocalDate> history = result.historyTariffIds();
        assertTrue(history.containsKey(10)); // history2 with counter 10
        assertTrue(history.containsKey(8)); // history4 with counter 8
        assertTrue(history.containsKey(7)); // history6 with counter 7
        assertTrue(history.containsKey(5)); // history1 with counter 5
        assertTrue(history.containsKey(3)); // history3 with counter 3

        verify(userRepo).findByUsername("business_user");
        verify(historyRepo).findByUser(businessUser);
    }

    @Test
    void getBusinessDetails_WithLessThanFiveHistoryEntries_Success() {
        // Arrange
        List<History> shortHistoryList = Arrays.asList(historyList.get(0), historyList.get(1));
        when(userRepo.findByUsername("business_user")).thenReturn(Optional.of(businessUser));
        when(historyRepo.findByUser(businessUser)).thenReturn(shortHistoryList);

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("business_user");

        // Assert
        assertNotNull(result);
        assertEquals("USA", result.originCountry());
        
        // Check tariffs instead of separate itemsSold and destinationCountries
        assertNotNull(result.tariffs());
        assertEquals(3, result.tariffs().size());
        
        List<String> reportingCountries = result.tariffs().stream()
                .map(BusinessTariffDTO::reportingCountry)
                .toList();
        List<String> items = result.tariffs().stream()
                .map(BusinessTariffDTO::item)
                .toList();
        
        assertTrue(reportingCountries.containsAll(Arrays.asList("Canada", "Mexico", "UK")));
        assertTrue(items.containsAll(Arrays.asList("Electronics", "Software", "Hardware")));
        
        assertNotNull(result.historyTariffIds());
        assertEquals(2, result.historyTariffIds().size()); // Should return only 2

        verify(userRepo).findByUsername("business_user");
        verify(historyRepo).findByUser(businessUser);
    }

    @Test
    void getBusinessDetails_WithEmptyHistory_Success() {
        // Arrange
        when(userRepo.findByUsername("business_user")).thenReturn(Optional.of(businessUser));
        when(historyRepo.findByUser(businessUser)).thenReturn(new ArrayList<>());

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("business_user");

        // Assert
        assertNotNull(result);
        assertEquals("USA", result.originCountry());
        
        // Check tariffs instead of separate itemsSold and destinationCountries
        assertNotNull(result.tariffs());
        assertEquals(3, result.tariffs().size());
        
        List<String> reportingCountries = result.tariffs().stream()
                .map(BusinessTariffDTO::reportingCountry)
                .toList();
        List<String> items = result.tariffs().stream()
                .map(BusinessTariffDTO::item)
                .toList();
        
        assertTrue(reportingCountries.containsAll(Arrays.asList("Canada", "Mexico", "UK")));
        assertTrue(items.containsAll(Arrays.asList("Electronics", "Software", "Hardware")));
        
        assertNotNull(result.historyTariffIds());
        assertEquals(0, result.historyTariffIds().size()); // Should return empty map

        verify(userRepo).findByUsername("business_user");
        verify(historyRepo).findByUser(businessUser);
    }

    @Test
    void getBusinessDetails_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.getBusinessDetails("nonexistent_user"));

        assertEquals("Unable to retrieve this account", exception.getMessage());
        verify(userRepo).findByUsername("nonexistent_user");
        verify(historyRepo, never()).findByUser(any());
    }

    @Test
    void getBusinessDetails_NotBusinessUser_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("regular_user")).thenReturn(Optional.of(nonBusinessUser));

        // Act & Assert
        IllegalAccessError exception = assertThrows(
                IllegalAccessError.class,
                () -> businessService.getBusinessDetails("regular_user"));

        assertEquals("The user is not a business user", exception.getMessage());
        verify(userRepo).findByUsername("regular_user");
        verify(historyRepo, never()).findByUser(any());
    }

    @Test
    void getBusinessDetails_VerifyHistorySorting() {
        // Arrange
        when(userRepo.findByUsername("business_user")).thenReturn(Optional.of(businessUser));
        when(historyRepo.findByUser(businessUser)).thenReturn(historyList);

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("business_user");

        // Assert
        Map<Integer, LocalDate> history = result.historyTariffIds();

        // Verify the mapping is counter -> date (as per the implementation)
        assertEquals(LocalDate.of(2023, 2, 1), history.get(10)); // Counter 10 -> Date from history2
        assertEquals(LocalDate.of(2023, 4, 1), history.get(8)); // Counter 8 -> Date from history4
        assertEquals(LocalDate.of(2023, 6, 1), history.get(7)); // Counter 7 -> Date from history6
        assertEquals(LocalDate.of(2023, 1, 1), history.get(5)); // Counter 5 -> Date from history1
        assertEquals(LocalDate.of(2023, 3, 1), history.get(3)); // Counter 3 -> Date from history3

        verify(userRepo).findByUsername("business_user");
        verify(historyRepo).findByUser(businessUser);
    }

    @Test
    void getBusinessDetails_WithDifferentBusinessData_Success() {
        // Arrange
        BusinessDetails detail1 = new BusinessDetails("USA", "Clothing");
        BusinessDetails detail2 = new BusinessDetails("France", "Accessories");
        
        BusinessUser anotherBusinessUser = new BusinessUser(
                "another_business_user",
                "pw",
                Role.BUSINESS,
                new HashSet<>(Set.of(detail1, detail2)),
                "Canada");

        when(userRepo.findByUsername("another_business_user")).thenReturn(Optional.of(anotherBusinessUser));
        when(historyRepo.findByUser(anotherBusinessUser)).thenReturn(new ArrayList<>());

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("another_business_user");

        // Assert
        assertNotNull(result);
        assertEquals("Canada", result.originCountry());
        
        // Check tariffs
        assertNotNull(result.tariffs());
        assertEquals(2, result.tariffs().size());
        
        List<String> reportingCountries = result.tariffs().stream()
                .map(BusinessTariffDTO::reportingCountry)
                .toList();
        List<String> items = result.tariffs().stream()
                .map(BusinessTariffDTO::item)
                .toList();
        
        assertTrue(reportingCountries.containsAll(Arrays.asList("USA", "France")));
        assertTrue(items.containsAll(Arrays.asList("Clothing", "Accessories")));
        
        assertNotNull(result.historyTariffIds());

        verify(userRepo).findByUsername("another_business_user");
        verify(historyRepo).findByUser(anotherBusinessUser);
    }

    @Test
    void getBusinessDetails_ExactlyFiveHistoryEntries_Success() {
        // Arrange
        List<History> exactFiveHistory = historyList.subList(0, 5);
        when(userRepo.findByUsername("business_user")).thenReturn(Optional.of(businessUser));
        when(historyRepo.findByUser(businessUser)).thenReturn(exactFiveHistory);

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("business_user");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.historyTariffIds().size());

        verify(userRepo).findByUsername("business_user");
        verify(historyRepo).findByUser(businessUser);
    }

    @Test
    void getBusinessDetails_WithEmptyTariffs_Success() {
        // Arrange
        BusinessUser businessUserEmpty = new BusinessUser(
                "business_user_empty",
                "pw",
                Role.BUSINESS,
                new HashSet<>(),
                "Germany");

        when(userRepo.findByUsername("business_user_empty")).thenReturn(Optional.of(businessUserEmpty));
        when(historyRepo.findByUser(businessUserEmpty)).thenReturn(new ArrayList<>());

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("business_user_empty");

        // Assert
        assertNotNull(result);
        assertEquals("Germany", result.originCountry());
        assertNotNull(result.tariffs());
        assertTrue(result.tariffs().isEmpty());
        assertNotNull(result.historyTariffIds());
        assertEquals(0, result.historyTariffIds().size());

        verify(userRepo).findByUsername("business_user_empty");
        verify(historyRepo).findByUser(businessUserEmpty);
    }

    @Test
    void getBusinessDetails_WithSingleTariff_Success() {
        // Arrange
        BusinessDetails singleDetail = new BusinessDetails("China", "Technology");
        BusinessUser singleItemBusinessUser = new BusinessUser(
                "single_item_business",
                "pw",
                Role.BUSINESS,
                new HashSet<>(Set.of(singleDetail)),
                "Japan");

        when(userRepo.findByUsername("single_item_business")).thenReturn(Optional.of(singleItemBusinessUser));
        when(historyRepo.findByUser(singleItemBusinessUser)).thenReturn(new ArrayList<>());

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("single_item_business");

        // Assert
        assertNotNull(result);
        assertEquals("Japan", result.originCountry());
        assertEquals(1, result.tariffs().size());
        
        BusinessTariffDTO tariff = result.tariffs().get(0);
        assertEquals("China", tariff.reportingCountry());
        assertEquals("Technology", tariff.item());

        verify(userRepo).findByUsername("single_item_business");
        verify(historyRepo).findByUser(singleItemBusinessUser);
    }

    @Test
    void addTariffRecord_Success_addsTariffAndSaves() {
        // Arrange
        BusinessTariffDTO tariff = new BusinessTariffDTO("Germany", "Widgets");
        BusinessDetails businessDetails = new BusinessDetails("Germany", "Widgets");
        
        when(userRepo.findByUsername("business_user")).thenReturn(Optional.of(businessUser));
        when(businessDetailsRepo.findByReportingCountryAndItemIgnoreCase("Germany", "Widgets"))
                .thenReturn(Optional.of(businessDetails));

        // Act
        businessService.addTariffRecord(tariff, "business_user");

        // Assert
        assertTrue(businessUser.getTariffData().contains(businessDetails));
        verify(userRepo).findByUsername("business_user");
        verify(businessDetailsRepo).findByReportingCountryAndItemIgnoreCase("Germany", "Widgets");
        verify(userRepo).save(businessUser);
    }

    @Test
    void addTariffRecord_UserNotFound_ThrowsException() {
        // Arrange
        BusinessTariffDTO tariff = new BusinessTariffDTO("Germany", "Widgets");
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.addTariffRecord(tariff, "ghost"));
        assertEquals("Unable to retrieve this account", ex.getMessage());
        verify(userRepo).findByUsername("ghost");
        verify(userRepo, never()).save(any());
    }

    @Test
    void addTariffRecord_NotBusinessUser_ThrowsException() {
        // Arrange
        BusinessTariffDTO tariff = new BusinessTariffDTO("Germany", "Widgets");
        when(userRepo.findByUsername("regular_user")).thenReturn(Optional.of(nonBusinessUser));

        // Act + Assert
        IllegalAccessError ex = assertThrows(
                IllegalAccessError.class,
                () -> businessService.addTariffRecord(tariff, "regular_user"));
        assertEquals("The user is not a business user", ex.getMessage());
        verify(userRepo).findByUsername("regular_user");
        verify(userRepo, never()).save(any());
    }

    @Test
    void deleteTariffRecord_Success_removesTariffAndSaves() {
        // Arrange
        BusinessTariffDTO tariff = new BusinessTariffDTO("Mexico", "Software");
        BusinessDetails businessDetails = new BusinessDetails("Mexico", "Software");
        businessUser.getTariffData().add(businessDetails);
        
        when(userRepo.findByUsername("business_user")).thenReturn(Optional.of(businessUser));
        when(businessDetailsRepo.findByReportingCountryAndItemIgnoreCase("Mexico", "Software"))
                .thenReturn(Optional.of(businessDetails));

        // Act
        businessService.deleteTariffRecord(tariff, "business_user");

        // Assert
        assertFalse(businessUser.getTariffData().contains(businessDetails));
        verify(userRepo).findByUsername("business_user");
        verify(businessDetailsRepo).findByReportingCountryAndItemIgnoreCase("Mexico", "Software");
        verify(userRepo).save(businessUser);
    }

    @Test
    void deleteTariffRecord_UserNotFound_ThrowsException() {
        // Arrange
        BusinessTariffDTO tariff = new BusinessTariffDTO("Mexico", "Software");
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.deleteTariffRecord(tariff, "ghost"));
        assertEquals("Unable to retrieve this account", ex.getMessage());
        verify(userRepo).findByUsername("ghost");
        verify(userRepo, never()).save(any());
    }

    @Test
    void deleteTariffRecord_NotBusinessUser_ThrowsException() {
        // Arrange
        BusinessTariffDTO tariff = new BusinessTariffDTO("Mexico", "Software");
        when(userRepo.findByUsername("regular_user")).thenReturn(Optional.of(nonBusinessUser));

        // Act + Assert
        IllegalAccessError ex = assertThrows(
                IllegalAccessError.class,
                () -> businessService.deleteTariffRecord(tariff, "regular_user"));
        assertEquals("The user is not a business user", ex.getMessage());
        verify(userRepo).findByUsername("regular_user");
        verify(userRepo, never()).save(any());
    }
}
