package com.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import com.user.history.History;
import com.user.history.HistoryRepo;
import com.user.user.User;
import com.user.user.UserRepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepo userRepo;

	@Mock
	private HistoryRepo historyRepo;

	@InjectMocks
	private UserServiceImpl userService;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1);
		user.setUsername("alice");
	}

	// addHistory
	@Test
	@DisplayName("addHistory: throws when user not found")
	void addHistory_userNotFound_throws() {
		// Arrange
		when(userRepo.findByUsername("missing"))
				.thenReturn(Optional.empty());

		// Act + Assert
		assertThrows(IllegalArgumentException.class,
				() -> userService.addHistory("missing", 101));

		verify(userRepo).findByUsername("missing");
		verifyNoMoreInteractions(userRepo, historyRepo);
	}

	@Test
	@DisplayName("addHistory: new tariff creates History and returns top 5 by counter")
	void addHistory_newEntry_savesAndReturnsTop5() {
		// Arrange
		when(userRepo.findByUsername("alice")).thenReturn(Optional.of(user));
		when(historyRepo.findByTariffIdAndUser(200, user)).thenReturn(Optional.empty());

		// Save of new entry
		when(historyRepo.save(any(History.class))).thenAnswer(inv -> inv.getArgument(0));

		// Prepare existing history list (6 entries to test top-5 cap). Distinct counters.
		List<History> existing = new ArrayList<>();
		existing.add(historyWith(user, 1, 10)); // tariffId=1, counter=10
		existing.add(historyWith(user, 2, 8));
		existing.add(historyWith(user, 3, 7));
		existing.add(historyWith(user, 4, 2));
		existing.add(historyWith(user, 5, 1));
		existing.add(historyWith(user, 6, 20));
		when(historyRepo.findByUser(user)).thenReturn(existing);

		// Act
		Map<Integer, LocalDate> result = userService.addHistory("alice", 200);

		// Assert
		// Saved once for the new tariff
		ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
		verify(historyRepo).save(captor.capture());
		History saved = captor.getValue();
		assertEquals(200, saved.getTariffId());
		assertEquals(user, saved.getUser());
		assertEquals(1, saved.getCounter());
		assertEquals(LocalDate.now(), saved.getLocalDate());

		// Top 5 by counter: tariffIds [6,1,2,3,4]
		List<Integer> expectedOrder = Arrays.asList(6, 1, 2, 3, 4);
		assertEquals(5, result.size());
		assertEquals(expectedOrder, new ArrayList<>(result.keySet()));

		verify(userRepo).findByUsername("alice");
		verify(historyRepo).findByTariffIdAndUser(200, user);
		verify(historyRepo).findByUser(user);
		verifyNoMoreInteractions(userRepo, historyRepo);
	}

	@Test
	@DisplayName("addHistory: existing tariff increments counter, updates date, returns top 5")
	void addHistory_existingEntry_updatesAndReturnsTop5() {
		// Arrange
		when(userRepo.findByUsername("alice")).thenReturn(Optional.of(user));

		History existing = historyWith(user, 300, 3);
		existing.setLocalDate(LocalDate.now().minusDays(5));
		when(historyRepo.findByTariffIdAndUser(300, user)).thenReturn(Optional.of(existing));

		when(historyRepo.save(any(History.class))).thenAnswer(inv -> inv.getArgument(0));

		List<History> list = new ArrayList<>();
		list.add(historyWith(user, 9, 11));
		list.add(historyWith(user, 8, 9));
		list.add(historyWith(user, 7, 5));
		list.add(historyWith(user, 6, 3));
		list.add(historyWith(user, 5, 2));
		list.add(historyWith(user, 4, 1));
		when(historyRepo.findByUser(user)).thenReturn(list);

		// Act
		Map<Integer, LocalDate> result = userService.addHistory("alice", 300);

		// Assert: saved entity has counter+1 and date=now
		ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
		verify(historyRepo).save(captor.capture());
		History saved = captor.getValue();
		assertEquals(4, saved.getCounter());
		assertEquals(LocalDate.now(), saved.getLocalDate());

		// Top 5 order: [9,8,7,6,5]
		List<Integer> expectedOrder = Arrays.asList(9, 8, 7, 6, 5);
		assertEquals(expectedOrder, new ArrayList<>(result.keySet()));

		verify(userRepo).findByUsername("alice");
		verify(historyRepo).findByTariffIdAndUser(300, user);
		verify(historyRepo).findByUser(user);
		verifyNoMoreInteractions(userRepo, historyRepo);
	}

	// retrieveHistory
	@Test
	@DisplayName("retrieveHistory: throws when user not found")
	void retrieveHistory_userNotFound_throws() {
		// Arrange
		when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

		// Act + Assert
		assertThrows(IllegalArgumentException.class,
				() -> userService.retrieveHistory("ghost"));

		verify(userRepo).findByUsername("ghost");
		verifyNoMoreInteractions(userRepo, historyRepo);
	}

	@Test
	@DisplayName("retrieveHistory: returns top 5 sorted by counter desc")
	void retrieveHistory_returnsTop5Sorted() {
		// Arrange
		when(userRepo.findByUsername("alice")).thenReturn(Optional.of(user));
		List<History> list = new ArrayList<>();
		list.add(historyWith(user, 10, 100));
		list.add(historyWith(user, 11, 50));
		list.add(historyWith(user, 12, 30));
		list.add(historyWith(user, 13, 20));
		list.add(historyWith(user, 14, 10));
		list.add(historyWith(user, 15, 1));
		when(historyRepo.findByUser(user)).thenReturn(list);

		// Act
		Map<Integer, LocalDate> result = userService.retrieveHistory("alice");

		// Assert
		List<Integer> expectedOrder = Arrays.asList(10, 11, 12, 13, 14);
		assertEquals(5, result.size());
		assertEquals(expectedOrder, new ArrayList<>(result.keySet()));

		verify(userRepo).findByUsername("alice");
		verify(historyRepo).findByUser(user);
		verifyNoMoreInteractions(userRepo, historyRepo);
	}

	// getAllUsers
	@Test
	@DisplayName("getAllUsers: delegates to repository")
	void getAllUsers_returnsList() {
		// Arrange
		User u1 = new User(); u1.setId(1); u1.setUsername("a");
		User u2 = new User(); u2.setId(2); u2.setUsername("b");
		when(userRepo.findAll()).thenReturn(Arrays.asList(u1, u2));

		// Act
		List<User> users = userService.getAllUsers();

		// Assert
		assertEquals(2, users.size());
		assertEquals("a", users.get(0).getUsername());
		assertEquals("b", users.get(1).getUsername());
		verify(userRepo).findAll();
		verifyNoMoreInteractions(userRepo, historyRepo);
	}

	// Helpers
	private static History historyWith(User user, int tariffId, int counter) {
		History h = new History(tariffId, user);
		h.setCounter(counter);
		h.setLocalDate(LocalDate.now());
		return h;
	}
}

