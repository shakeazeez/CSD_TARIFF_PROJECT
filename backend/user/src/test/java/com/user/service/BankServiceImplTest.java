package com.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.user.dto.BankInfoDTO;
import com.user.enums.Industry;
import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.BankUser;
import com.user.user.User;
import com.user.user.UserRepo;

@ExtendWith(MockitoExtension.class)
class BankServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private HistoryRepo historyRepo;

    @InjectMocks
    private BankServiceImpl bankService;

    private BankUser bankUser;
    private User nonBankUser;
    private List<History> historyList;

    @BeforeEach
    void setUp() {
        bankUser = new BankUser();
        bankUser.setUsername("bank_user");
        bankUser.setIndustry(Industry.TECHNOLOGY);
        bankUser.setOriginCountry("USA");

        nonBankUser = new User();
        nonBankUser.setUsername("regular_user");

        // Create history entries with different counters and dates
        History history1 = new History();
        history1.setTariffId(1);
        history1.setCounter(5);
        history1.setLocalDate(LocalDate.of(2023, 1, 1));
        history1.setUser(bankUser);

        History history2 = new History();
        history2.setTariffId(2);
        history2.setCounter(10);
        history2.setLocalDate(LocalDate.of(2023, 2, 1));
        history2.setUser(bankUser);

        History history3 = new History();
        history3.setTariffId(3);
        history3.setCounter(3);
        history3.setLocalDate(LocalDate.of(2023, 3, 1));
        history3.setUser(bankUser);

        History history4 = new History();
        history4.setTariffId(4);
        history4.setCounter(8);
        history4.setLocalDate(LocalDate.of(2023, 4, 1));
        history4.setUser(bankUser);

        History history5 = new History();
        history5.setTariffId(5);
        history5.setCounter(2);
        history5.setLocalDate(LocalDate.of(2023, 5, 1));
        history5.setUser(bankUser);

        History history6 = new History();
        history6.setTariffId(6);
        history6.setCounter(7);
        history6.setLocalDate(LocalDate.of(2023, 6, 1));
        history6.setUser(bankUser);

        historyList = Arrays.asList(history1, history2, history3, history4, history5, history6);
    }

    @Test
    void getBankInfo_Success() {
        // Arrange
        when(userRepo.findByUsername("bank_user")).thenReturn(Optional.of(bankUser));
        when(historyRepo.findByUser(bankUser)).thenReturn(historyList);

        // Act
        BankInfoDTO result = bankService.getBankInfo("bank_user");

        // Assert
        assertNotNull(result);
        assertEquals("TECHNOLOGY", result.industry());
        assertEquals("USA", result.originCountry());
        assertNotNull(result.historyTariffIds());
        assertEquals(5, result.historyTariffIds().size()); // Should return top 5

        // Verify the top 5 entries are returned in order of counter (descending)
        Map<Integer, LocalDate> history = result.historyTariffIds();
        assertTrue(history.containsKey(10)); // history2 with counter 10
        assertTrue(history.containsKey(8));  // history4 with counter 8
        assertTrue(history.containsKey(7));  // history6 with counter 7
        assertTrue(history.containsKey(5));  // history1 with counter 5
        assertTrue(history.containsKey(3));  // history3 with counter 3

        verify(userRepo).findByUsername("bank_user");
        verify(historyRepo).findByUser(bankUser);
    }

    @Test
    void getBankInfo_WithLessThanFiveHistoryEntries_Success() {
        // Arrange
        List<History> shortHistoryList = Arrays.asList(historyList.get(0), historyList.get(1));
        when(userRepo.findByUsername("bank_user")).thenReturn(Optional.of(bankUser));
        when(historyRepo.findByUser(bankUser)).thenReturn(shortHistoryList);

        // Act
        BankInfoDTO result = bankService.getBankInfo("bank_user");

        // Assert
        assertNotNull(result);
        assertEquals("TECHNOLOGY", result.industry());
        assertEquals("USA", result.originCountry());
        assertNotNull(result.historyTariffIds());
        assertEquals(2, result.historyTariffIds().size()); // Should return only 2

        verify(userRepo).findByUsername("bank_user");
        verify(historyRepo).findByUser(bankUser);
    }

    @Test
    void getBankInfo_WithEmptyHistory_Success() {
        // Arrange
        when(userRepo.findByUsername("bank_user")).thenReturn(Optional.of(bankUser));
        when(historyRepo.findByUser(bankUser)).thenReturn(new ArrayList<>());

        // Act
        BankInfoDTO result = bankService.getBankInfo("bank_user");

        // Assert
        assertNotNull(result);
        assertEquals("TECHNOLOGY", result.industry());
        assertEquals("USA", result.originCountry());
        assertNotNull(result.historyTariffIds());
        assertEquals(0, result.historyTariffIds().size()); // Should return empty map

        verify(userRepo).findByUsername("bank_user");
        verify(historyRepo).findByUser(bankUser);
    }

    @Test
    void getBankInfo_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bankService.getBankInfo("nonexistent_user")
        );

        assertEquals("Unable to retrieve this account", exception.getMessage());
        verify(userRepo).findByUsername("nonexistent_user");
        verify(historyRepo, never()).findByUser(any());
    }

    @Test
    void getBankInfo_NotBankUser_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("regular_user")).thenReturn(Optional.of(nonBankUser));

        // Act & Assert
        IllegalAccessError exception = assertThrows(
            IllegalAccessError.class,
            () -> bankService.getBankInfo("regular_user")
        );

        assertEquals("The user is not a bank user", exception.getMessage());
        verify(userRepo).findByUsername("regular_user");
        verify(historyRepo, never()).findByUser(any());
    }

    @Test
    void getBankInfo_VerifyHistorySorting() {
        // Arrange
        when(userRepo.findByUsername("bank_user")).thenReturn(Optional.of(bankUser));
        when(historyRepo.findByUser(bankUser)).thenReturn(historyList);

        // Act
        BankInfoDTO result = bankService.getBankInfo("bank_user");

        // Assert
        Map<Integer, LocalDate> history = result.historyTariffIds();
        
        // Verify the mapping is counter -> date (as per the implementation)
        assertEquals(LocalDate.of(2023, 2, 1), history.get(10)); // Counter 10 -> Date from history2
        assertEquals(LocalDate.of(2023, 4, 1), history.get(8));  // Counter 8 -> Date from history4
        assertEquals(LocalDate.of(2023, 6, 1), history.get(7));  // Counter 7 -> Date from history6
        assertEquals(LocalDate.of(2023, 1, 1), history.get(5));  // Counter 5 -> Date from history1
        assertEquals(LocalDate.of(2023, 3, 1), history.get(3));  // Counter 3 -> Date from history3

        verify(userRepo).findByUsername("bank_user");
        verify(historyRepo).findByUser(bankUser);
    }

    @Test
    void getBankInfo_WithDifferentIndustry_Success() {
        // Arrange
        BankUser bankUserFinance = new BankUser();
        bankUserFinance.setUsername("bank_user_finance");
        bankUserFinance.setIndustry(Industry.FINANCE);
        bankUserFinance.setOriginCountry("Canada");

        when(userRepo.findByUsername("bank_user_finance")).thenReturn(Optional.of(bankUserFinance));
        when(historyRepo.findByUser(bankUserFinance)).thenReturn(new ArrayList<>());

        // Act
        BankInfoDTO result = bankService.getBankInfo("bank_user_finance");

        // Assert
        assertNotNull(result);
        assertEquals("FINANCE", result.industry());
        assertEquals("Canada", result.originCountry());
        assertNotNull(result.historyTariffIds());

        verify(userRepo).findByUsername("bank_user_finance");
        verify(historyRepo).findByUser(bankUserFinance);
    }

    @Test
    void getBankInfo_ExactlyFiveHistoryEntries_Success() {
        // Arrange
        List<History> exactFiveHistory = historyList.subList(0, 5);
        when(userRepo.findByUsername("bank_user")).thenReturn(Optional.of(bankUser));
        when(historyRepo.findByUser(bankUser)).thenReturn(exactFiveHistory);

        // Act
        BankInfoDTO result = bankService.getBankInfo("bank_user");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.historyTariffIds().size());

        verify(userRepo).findByUsername("bank_user");
        verify(historyRepo).findByUser(bankUser);
    }
}
