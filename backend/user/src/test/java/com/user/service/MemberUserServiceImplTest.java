package com.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.user.dto.MemberInfoDTO;
import com.user.user.MemberUser;
import com.user.user.MemberUserRepo;
import com.user.user.User;
import com.user.user.UserRepo;

@ExtendWith(MockitoExtension.class)
class MemberUserServiceImplTest {

    @Mock
    private MemberUserRepo memberUserRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private MemberUserServiceImpl memberUserService;

    private MemberUser memberUser;
    private User nonMemberUser;

    @BeforeEach
    void setUp() {
        memberUser = new MemberUser();
        memberUser.setUsername("member_user");
        memberUser.setPinnedTariffId(new ArrayList<>(Arrays.asList(1, 2)));

        nonMemberUser = new User();
        nonMemberUser.setUsername("regular_user");
    }

    @Test
    void addPinnedTariff_Success() {
        // Arrange
        when(userRepo.findByUsername("member_user")).thenReturn(Optional.of(memberUser));
        when(memberUserRepo.save(any(MemberUser.class))).thenReturn(memberUser);

        // Act
        List<Integer> result = memberUserService.addPinnedTariff("member_user", 3);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));

        verify(userRepo).findByUsername("member_user");
        verify(memberUserRepo).save(memberUser);
    }

    @Test
    void addPinnedTariff_AlreadyPinned_DoesNotAddDuplicate() {
        // Arrange
        when(userRepo.findByUsername("member_user")).thenReturn(Optional.of(memberUser));

        // Act
        List<Integer> result = memberUserService.addPinnedTariff("member_user", 1); // Already pinned

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Should remain 2
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));

        verify(userRepo).findByUsername("member_user");
        verify(memberUserRepo, never()).save(any()); // Should not save if already pinned
    }

    @Test
    void addPinnedTariff_MaximumReached_ThrowsException() {
        // Arrange
        memberUser.setPinnedTariffId(new ArrayList<>(Arrays.asList(1, 2, 3))); // Already has 3
        when(userRepo.findByUsername("member_user")).thenReturn(Optional.of(memberUser));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> memberUserService.addPinnedTariff("member_user", 4)
        );

        assertEquals("Cannot pin more than 3 tariffs", exception.getMessage());
        verify(userRepo).findByUsername("member_user");
        verify(memberUserRepo, never()).save(any());
    }

    @Test
    void addPinnedTariff_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberUserService.addPinnedTariff("nonexistent_user", 1)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepo).findByUsername("nonexistent_user");
        verify(memberUserRepo, never()).save(any());
    }

    @Test
    void addPinnedTariff_NotMemberUser_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("regular_user")).thenReturn(Optional.of(nonMemberUser));

        // Act & Assert
        IllegalAccessError exception = assertThrows(
            IllegalAccessError.class,
            () -> memberUserService.addPinnedTariff("regular_user", 1)
        );

        assertEquals("This feature is not available for this user", exception.getMessage());
        verify(userRepo).findByUsername("regular_user");
        verify(memberUserRepo, never()).save(any());
    }

    @Test
    void removePinnedTariff_Success() {
        // Arrange
        when(userRepo.findByUsername("member_user")).thenReturn(Optional.of(memberUser));
        when(memberUserRepo.save(any(MemberUser.class))).thenReturn(memberUser);

        // Act
        List<Integer> result = memberUserService.removePinnedTariff("member_user", 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.contains(1));
        assertTrue(result.contains(2));

        verify(userRepo).findByUsername("member_user");
        verify(memberUserRepo).save(memberUser);
    }

    @Test
    void removePinnedTariff_TariffNotPinned_NoChange() {
        // Arrange
        when(userRepo.findByUsername("member_user")).thenReturn(Optional.of(memberUser));

        // Act
        List<Integer> result = memberUserService.removePinnedTariff("member_user", 5); // Not pinned

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Should remain unchanged
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));

        verify(userRepo).findByUsername("member_user");
        verify(memberUserRepo, never()).save(any()); // Should not save if tariff not pinned
    }

    @Test
    void removePinnedTariff_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberUserService.removePinnedTariff("nonexistent_user", 1)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepo).findByUsername("nonexistent_user");
        verify(memberUserRepo, never()).save(any());
    }

    @Test
    void removePinnedTariff_NotMemberUser_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("regular_user")).thenReturn(Optional.of(nonMemberUser));

        // Act & Assert
        IllegalAccessError exception = assertThrows(
            IllegalAccessError.class,
            () -> memberUserService.removePinnedTariff("regular_user", 1)
        );

        assertEquals("This feature is not available for this user", exception.getMessage());
        verify(userRepo).findByUsername("regular_user");
        verify(memberUserRepo, never()).save(any());
    }

    @Test
    void getPinnedTariff_Success() {
        // Arrange
        when(userRepo.findByUsername("member_user")).thenReturn(Optional.of(memberUser));

        // Act
        MemberInfoDTO result = memberUserService.getPinnedTariff("member_user");

        // Assert
        assertNotNull(result);
        assertNotNull(result.pinnedTariffs());
        assertEquals(2, result.pinnedTariffs().size());
        assertTrue(result.pinnedTariffs().contains(1));
        assertTrue(result.pinnedTariffs().contains(2));

        verify(userRepo).findByUsername("member_user");
    }

    @Test
    void getPinnedTariff_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> memberUserService.getPinnedTariff("nonexistent_user")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepo).findByUsername("nonexistent_user");
    }

    @Test
    void getPinnedTariff_NotMemberUser_ThrowsException() {
        // Arrange
        when(userRepo.findByUsername("regular_user")).thenReturn(Optional.of(nonMemberUser));

        // Act & Assert
        IllegalAccessError exception = assertThrows(
            IllegalAccessError.class,
            () -> memberUserService.getPinnedTariff("regular_user")
        );

        assertEquals("Not a general user", exception.getMessage());
        verify(userRepo).findByUsername("regular_user");
    }

    @Test
    void addPinnedTariff_EmptyList_Success() {
        // Arrange
        MemberUser emptyMemberUser = new MemberUser();
        emptyMemberUser.setUsername("empty_user");
        emptyMemberUser.setPinnedTariffId(new ArrayList<>());
        
        when(userRepo.findByUsername("empty_user")).thenReturn(Optional.of(emptyMemberUser));
        when(memberUserRepo.save(any(MemberUser.class))).thenReturn(emptyMemberUser);

        // Act
        List<Integer> result = memberUserService.addPinnedTariff("empty_user", 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(1));

        verify(userRepo).findByUsername("empty_user");
        verify(memberUserRepo).save(emptyMemberUser);
    }

    @Test
    void removePinnedTariff_EmptyList_NoChange() {
        // Arrange
        MemberUser emptyMemberUser = new MemberUser();
        emptyMemberUser.setUsername("empty_user");
        emptyMemberUser.setPinnedTariffId(new ArrayList<>());
        
        when(userRepo.findByUsername("empty_user")).thenReturn(Optional.of(emptyMemberUser));

        // Act
        List<Integer> result = memberUserService.removePinnedTariff("empty_user", 1);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(userRepo).findByUsername("empty_user");
        verify(memberUserRepo, never()).save(any());
    }
}
