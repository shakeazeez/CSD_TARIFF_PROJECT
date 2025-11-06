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

import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.User;
import com.user.user.UserRepo;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private HistoryRepo historyRepo;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private History existingHistory;
    private List<History> historyList;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("test_user");
        user.setId(1);

        existingHistory = new History();
        existingHistory.setTariffId(100);
        existingHistory.setCounter(3);
        existingHistory.setLocalDate(LocalDate.of(2023, 1, 1));
        existingHistory.setUser(user);

        // Create a list of history entries for testing retrieval
        History history1 = new History();
        history1.setTariffId(1);
        history1.setCounter(5);
        history1.setLocalDate(LocalDate.of(2023, 1, 1));

        History history2 = new History();
        history2.setTariffId(2);
        history2.setCounter(10);
        history2.setLocalDate(LocalDate.of(2023, 2, 1));

        History history3 = new History();
        history3.setTariffId(3);
        history3.setCounter(3);
        history3.setLocalDate(LocalDate.of(2023, 3, 1));

        History history4 = new History();
        history4.setTariffId(4);
        history4.setCounter(8);
        history4.setLocalDate(LocalDate.of(2023, 4, 1));

        History history5 = new History();
        history5.setTariffId(5);
        history5.setCounter(2);
        history5.setLocalDate(LocalDate.of(2023, 5, 1));

        History history6 = new History();
        history6.setTariffId(6);
        history6.setCounter(7);
        history6.setLocalDate(LocalDate.of(2023, 6, 1));

        historyList = Arrays.asList(history1, history2, history3, history4, history5, history6);
    }

    @Test
    void addHistory_NewTariff_Success() {
        // Arrange
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByTariffIdAndUser(200, user)).thenReturn(Optional.empty());
        when(historyRepo.findByUser(user)).thenReturn(historyList);

        // Act
        Map<Integer, LocalDate> result = userService.addHistory("test_user", 200);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size()); // Should return top 5

        verify(userRepo).findByUsername("test_user");
        verify(historyRepo).findByTariffIdAndUser(200, user);
        verify(historyRepo).save(any(History.class)); // Should save new history entry
        verify(historyRepo).findByUser(user);
    }

    @Test
    void addHistory_ExistingTariff_UpdatesCounter() {
        // Arrange
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByTariffIdAndUser(100, user)).thenReturn(Optional.of(existingHistory));
        when(historyRepo.findByUser(user)).thenReturn(historyList);

        // Act
        Map<Integer, LocalDate> result = userService.addHistory("test_user", 100);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size()); // Should return top 5

        // Verify that the existing history was updated
        assertEquals(4, existingHistory.getCounter()); // Counter should be incremented
        assertEquals(LocalDate.now(), existingHistory.getLocalDate()); // Date should be updated

        verify(userRepo).findByUsername("test_user");
        verify(historyRepo).findByTariffIdAndUser(100, user);
        verify(historyRepo).save(existingHistory); // Should save updated history entry
        verify(historyRepo).findByUser(user);
    }

    @Test
    void addHistory_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.addHistory("nonexistent_user", 100)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepo).findByUsername("nonexistent_user");
        verify(historyRepo, never()).findByTariffIdAndUser(anyInt(), any());
        verify(historyRepo, never()).save(any());
    }

    @Test
    void addHistory_ReturnsTop5InCorrectOrder() {
        // Arrange
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByTariffIdAndUser(200, user)).thenReturn(Optional.empty());
        when(historyRepo.findByUser(user)).thenReturn(historyList);

        // Act
        Map<Integer, LocalDate> result = userService.addHistory("test_user", 200);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());

        // Verify that the map is ordered by counter (descending)
        List<Integer> tariffIds = new ArrayList<>(result.keySet());
        assertEquals(Integer.valueOf(2), tariffIds.get(0)); // Counter 10
        assertEquals(Integer.valueOf(4), tariffIds.get(1)); // Counter 8
        assertEquals(Integer.valueOf(6), tariffIds.get(2)); // Counter 7
        assertEquals(Integer.valueOf(1), tariffIds.get(3)); // Counter 5
        assertEquals(Integer.valueOf(3), tariffIds.get(4)); // Counter 3
    }

    @Test
    void retrieveHistory_Success() {
        // Arrange
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByUser(user)).thenReturn(historyList);

        // Act
        Map<Integer, LocalDate> result = userService.retrieveHistory("test_user");

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size()); // Should return top 5

        // Verify that the map is ordered by counter (descending)
        List<Integer> tariffIds = new ArrayList<>(result.keySet());
        assertEquals(Integer.valueOf(2), tariffIds.get(0)); // Counter 10
        assertEquals(Integer.valueOf(4), tariffIds.get(1)); // Counter 8
        assertEquals(Integer.valueOf(6), tariffIds.get(2)); // Counter 7
        assertEquals(Integer.valueOf(1), tariffIds.get(3)); // Counter 5
        assertEquals(Integer.valueOf(3), tariffIds.get(4)); // Counter 3

        verify(userRepo).findByUsername("test_user");
        verify(historyRepo).findByUser(user);
    }

    @Test
    void retrieveHistory_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.retrieveHistory("nonexistent_user")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepo).findByUsername("nonexistent_user");
        verify(historyRepo, never()).findByUser(any());
    }

    @Test
    void retrieveHistory_WithLessThanFiveEntries_Success() {
        // Arrange
        List<History> shortHistoryList = Arrays.asList(historyList.get(0), historyList.get(1));
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByUser(user)).thenReturn(shortHistoryList);

        // Act
        Map<Integer, LocalDate> result = userService.retrieveHistory("test_user");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Should return only available entries

        verify(userRepo).findByUsername("test_user");
        verify(historyRepo).findByUser(user);
    }

    @Test
    void retrieveHistory_WithEmptyHistory_Success() {
        // Arrange
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByUser(user)).thenReturn(new ArrayList<>());

        // Act
        Map<Integer, LocalDate> result = userService.retrieveHistory("test_user");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size()); // Should return empty map

        verify(userRepo).findByUsername("test_user");
        verify(historyRepo).findByUser(user);
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> userList = Arrays.asList(
            new User(),
            new User(),
            new User()
        );
        when(userRepo.findAll()).thenReturn(userList);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(userList, result);

        verify(userRepo).findAll();
    }

    @Test
    void getAllUsers_EmptyList_Success() {
        // Arrange
        when(userRepo.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(userRepo).findAll();
    }

    @Test
    void addHistory_VerifyNewHistoryCreation() {
        // Arrange
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByTariffIdAndUser(999, user)).thenReturn(Optional.empty());
        when(historyRepo.findByUser(user)).thenReturn(new ArrayList<>());

        // Act
        userService.addHistory("test_user", 999);

        // Assert
        verify(historyRepo).save(argThat(history -> {
            assertEquals(Integer.valueOf(999), history.getTariffId());
            assertEquals(user, history.getUser());
            return true;
        }));
    }

    @Test
    void addHistory_VerifyCounterIncrement() {
        // Arrange
        History historyToUpdate = new History();
        historyToUpdate.setTariffId(300);
        historyToUpdate.setCounter(5);
        historyToUpdate.setLocalDate(LocalDate.of(2023, 1, 1));
        historyToUpdate.setUser(user);

        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByTariffIdAndUser(300, user)).thenReturn(Optional.of(historyToUpdate));
        when(historyRepo.findByUser(user)).thenReturn(Arrays.asList(historyToUpdate));

        // Act
        userService.addHistory("test_user", 300);

        // Assert
        assertEquals(6, historyToUpdate.getCounter()); // Should be incremented from 5 to 6
        assertEquals(LocalDate.now(), historyToUpdate.getLocalDate()); // Should be updated to today
        verify(historyRepo).save(historyToUpdate);
    }

    @Test
    void retrieveHistory_VerifyDateMapping() {
        // Arrange
        when(userRepo.findByUsername("test_user")).thenReturn(Optional.of(user));
        when(historyRepo.findByUser(user)).thenReturn(historyList);

        // Act
        Map<Integer, LocalDate> result = userService.retrieveHistory("test_user");

        // Assert
        // Verify specific date mappings
        assertEquals(LocalDate.of(2023, 2, 1), result.get(2)); // TariffId 2
        assertEquals(LocalDate.of(2023, 4, 1), result.get(4)); // TariffId 4
        assertEquals(LocalDate.of(2023, 6, 1), result.get(6)); // TariffId 6
        assertEquals(LocalDate.of(2023, 1, 1), result.get(1)); // TariffId 1
        assertEquals(LocalDate.of(2023, 3, 1), result.get(3)); // TariffId 3
    }
}
