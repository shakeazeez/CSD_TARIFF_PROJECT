package com.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.user.dto.BusinessInfoDTO;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BusinessUser;
import com.user.user.User;
import com.user.user.UserRepo;

@ExtendWith(MockitoExtension.class)
class BusinessServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private HistoryRepo historyRepo;

    @InjectMocks
    private BusinessServiceImpl businessService;

    private BusinessUser businessUser;
    private User nonBusinessUser;
    private List<History> historyList;

    @BeforeEach
    void setUp() {
        businessUser = new BusinessUser();
        businessUser.setUsername("business_user");
        businessUser.setOriginCountry("USA");
        businessUser.setItemsSold(Arrays.asList("Electronics", "Software", "Hardware"));
        businessUser.setDestinationCountries(Arrays.asList("Canada", "Mexico", "UK"));

        nonBusinessUser = new User();
        nonBusinessUser.setUsername("regular_user");

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
        assertEquals(Arrays.asList("Electronics", "Software", "Hardware"), result.itemSold());
        assertEquals(Arrays.asList("Canada", "Mexico", "UK"), result.destinationCountries());
        assertNotNull(result.historyTariffIds());
        assertEquals(5, result.historyTariffIds().size()); // Should return top 5

        // Verify the top 5 entries are returned in order of counter (descending)
        Map<Integer, LocalDate> history = result.historyTariffIds();
        assertTrue(history.containsKey(10)); // history2 with counter 10
        assertTrue(history.containsKey(8));  // history4 with counter 8
        assertTrue(history.containsKey(7));  // history6 with counter 7
        assertTrue(history.containsKey(5));  // history1 with counter 5
        assertTrue(history.containsKey(3));  // history3 with counter 3

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
        assertEquals(Arrays.asList("Electronics", "Software", "Hardware"), result.itemSold());
        assertEquals(Arrays.asList("Canada", "Mexico", "UK"), result.destinationCountries());
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
        assertEquals(Arrays.asList("Electronics", "Software", "Hardware"), result.itemSold());
        assertEquals(Arrays.asList("Canada", "Mexico", "UK"), result.destinationCountries());
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
            () -> businessService.getBusinessDetails("nonexistent_user")
        );

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
            () -> businessService.getBusinessDetails("regular_user")
        );

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
        assertEquals(LocalDate.of(2023, 4, 1), history.get(8));  // Counter 8 -> Date from history4
        assertEquals(LocalDate.of(2023, 6, 1), history.get(7));  // Counter 7 -> Date from history6
        assertEquals(LocalDate.of(2023, 1, 1), history.get(5));  // Counter 5 -> Date from history1
        assertEquals(LocalDate.of(2023, 3, 1), history.get(3));  // Counter 3 -> Date from history3

        verify(userRepo).findByUsername("business_user");
        verify(historyRepo).findByUser(businessUser);
    }

    @Test
    void getBusinessDetails_WithDifferentBusinessData_Success() {
        // Arrange
        BusinessUser anotherBusinessUser = new BusinessUser();
        anotherBusinessUser.setUsername("another_business_user");
        anotherBusinessUser.setOriginCountry("Canada");
        anotherBusinessUser.setItemsSold(Arrays.asList("Clothing", "Accessories"));
        anotherBusinessUser.setDestinationCountries(Arrays.asList("USA", "France"));

        when(userRepo.findByUsername("another_business_user")).thenReturn(Optional.of(anotherBusinessUser));
        when(historyRepo.findByUser(anotherBusinessUser)).thenReturn(new ArrayList<>());

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("another_business_user");

        // Assert
        assertNotNull(result);
        assertEquals("Canada", result.originCountry());
        assertEquals(Arrays.asList("Clothing", "Accessories"), result.itemSold());
        assertEquals(Arrays.asList("USA", "France"), result.destinationCountries());
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
    void getBusinessDetails_WithEmptyItemsSoldAndDestinations_Success() {
        // Arrange
        BusinessUser businessUserEmpty = new BusinessUser();
        businessUserEmpty.setUsername("business_user_empty");
        businessUserEmpty.setOriginCountry("Germany");
        businessUserEmpty.setItemsSold(new ArrayList<>());
        businessUserEmpty.setDestinationCountries(new ArrayList<>());

        when(userRepo.findByUsername("business_user_empty")).thenReturn(Optional.of(businessUserEmpty));
        when(historyRepo.findByUser(businessUserEmpty)).thenReturn(new ArrayList<>());

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("business_user_empty");

        // Assert
        assertNotNull(result);
        assertEquals("Germany", result.originCountry());
        assertNotNull(result.itemSold());
        assertTrue(result.itemSold().isEmpty());
        assertNotNull(result.destinationCountries());
        assertTrue(result.destinationCountries().isEmpty());
        assertNotNull(result.historyTariffIds());
        assertEquals(0, result.historyTariffIds().size());

        verify(userRepo).findByUsername("business_user_empty");
        verify(historyRepo).findByUser(businessUserEmpty);
    }

    @Test
    void getBusinessDetails_WithSingleItem_Success() {
        // Arrange
        BusinessUser singleItemBusinessUser = new BusinessUser();
        singleItemBusinessUser.setUsername("single_item_business");
        singleItemBusinessUser.setOriginCountry("Japan");
        singleItemBusinessUser.setItemsSold(Arrays.asList("Technology"));
        singleItemBusinessUser.setDestinationCountries(Arrays.asList("China"));

        when(userRepo.findByUsername("single_item_business")).thenReturn(Optional.of(singleItemBusinessUser));
        when(historyRepo.findByUser(singleItemBusinessUser)).thenReturn(new ArrayList<>());

        // Act
        BusinessInfoDTO result = businessService.getBusinessDetails("single_item_business");

        // Assert
        assertNotNull(result);
        assertEquals("Japan", result.originCountry());
        assertEquals(1, result.itemSold().size());
        assertEquals("Technology", result.itemSold().get(0));
        assertEquals(1, result.destinationCountries().size());
        assertEquals("China", result.destinationCountries().get(0));

        verify(userRepo).findByUsername("single_item_business");
        verify(historyRepo).findByUser(singleItemBusinessUser);
    }
}
