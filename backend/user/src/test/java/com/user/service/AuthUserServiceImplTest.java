package com.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.user.dto.CreateUserDTO;
import com.user.dto.TokenDTO;
import com.user.enums.Role;
import com.user.user.BankUser;
import com.user.user.BusinessUser;
import com.user.user.User;
import com.user.user.UserRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthUserService Unit Tests")
class AuthUserServiceImplTest {

    @Mock
    private UserRepo generalUserRepo;

    @InjectMocks
    private AuthUserServiceImpl authUserService;

    private CreateUserDTO memberUserDTO;
    private CreateUserDTO bankUserDTO;
    private CreateUserDTO businessUserDTO;
    private CreateUserDTO adminUserDTO;

    @BeforeEach
    void setUp() {
        memberUserDTO = new CreateUserDTO(
            "member_user",
            "password123",
            "MEMBER",
            null,
            null,
            null,
            null
        );

        bankUserDTO = new CreateUserDTO(
            "bank_user",
            "password123",
            "BANK",
            "TECHNOLOGY",
            "USA",
            null,
            null
        );

        List<String> itemsSold = Arrays.asList("Electronics", "Software");
        List<String> destinationCountries = Arrays.asList("Canada", "Mexico");
        businessUserDTO = new CreateUserDTO(
            "business_user",
            "password123",
            "BUSINESS",
            null,
            "USA",
            destinationCountries,
            itemsSold
        );

        adminUserDTO = new CreateUserDTO(
            "admin_user",
            "password123",
            "ADMIN",
            null,
            null,
            null,
            null
        );
    }

    @Test
    void createUser_MemberUser_Success() {
        // Arrange
        when(generalUserRepo.findByUsername("member_user")).thenReturn(Optional.empty());

        // Act
        TokenDTO result = authUserService.createUser(memberUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals("member_user", result.getUsername());
        assertNull(result.getToken());
        assertNotNull(result.getPin());
        assertTrue(result.getPin().isEmpty());
        assertNotNull(result.getHistoricalTariffId());
        assertTrue(result.getHistoricalTariffId().isEmpty());

        verify(generalUserRepo).findByUsername("member_user");
        verify(generalUserRepo).save(any(User.class));
    }

    @Test
    void createUser_BankUser_Success() {
        // Arrange
        when(generalUserRepo.findByUsername("bank_user")).thenReturn(Optional.empty());

        // Act
        TokenDTO result = authUserService.createUser(bankUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals("bank_user", result.getUsername());
        assertNull(result.getToken());
        assertEquals("TECHNOLOGY", result.getIndustry());
        assertEquals("USA", result.getOriginCountry());
        assertNotNull(result.getHistoricalTariffId());
        assertTrue(result.getHistoricalTariffId().isEmpty());

        verify(generalUserRepo).findByUsername("bank_user");
        verify(generalUserRepo).save(any(BankUser.class));
    }

    @Test
    void createUser_BusinessUser_Success() {
        // Arrange
        when(generalUserRepo.findByUsername("business_user")).thenReturn(Optional.empty());

        // Act
        TokenDTO result = authUserService.createUser(businessUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals("business_user", result.getUsername());
        assertNull(result.getToken());
        assertEquals(Arrays.asList("Electronics", "Software"), result.getItemsSold());
        assertEquals(Arrays.asList("Canada", "Mexico"), result.getDestinationCountries());
        assertEquals("USA", result.getOriginCountry());
        assertNotNull(result.getHistoricalTariffId());
        assertTrue(result.getHistoricalTariffId().isEmpty());

        verify(generalUserRepo).findByUsername("business_user");
        verify(generalUserRepo).save(any(BusinessUser.class));
    }

    @Test
    void createUser_AdminUser_Success() {
        // Arrange
        when(generalUserRepo.findByUsername("admin_user")).thenReturn(Optional.empty());

        // Act
        TokenDTO result = authUserService.createUser(adminUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals("admin_user", result.getUsername());
        assertNull(result.getToken());
        assertNotNull(result.getHistoricalTariffId());
        assertTrue(result.getHistoricalTariffId().isEmpty());

        verify(generalUserRepo).findByUsername("admin_user");
        verify(generalUserRepo).save(any(User.class));
    }

    @Test
    void createUser_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        User existingUser = new User();
        existingUser.setUsername("member_user");
        when(generalUserRepo.findByUsername("member_user")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authUserService.createUser(memberUserDTO)
        );

        assertEquals("User with that username already exists", exception.getMessage());
        verify(generalUserRepo).findByUsername("member_user");
        verify(generalUserRepo, never()).save(any());
    }

    @Test
    void createUser_BankUser_MissingIndustry_ThrowsException() {
        // Arrange
        CreateUserDTO invalidBankUserDTO = new CreateUserDTO(
            "bank_user",
            "password123",
            "BANK",
            null, // Missing industry
            "USA",
            null,
            null
        );
        when(generalUserRepo.findByUsername("bank_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authUserService.createUser(invalidBankUserDTO)
        );

        assertEquals("Not enough parameters valid for bank user", exception.getMessage());
        verify(generalUserRepo).findByUsername("bank_user");
        verify(generalUserRepo, never()).save(any());
    }

    @Test
    void createUser_BankUser_MissingOriginCountry_ThrowsException() {
        // Arrange
        CreateUserDTO invalidBankUserDTO = new CreateUserDTO(
            "bank_user",
            "password123",
            "BANK",
            "TECHNOLOGY",
            null, // Missing origin country
            null,
            null
        );
        when(generalUserRepo.findByUsername("bank_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authUserService.createUser(invalidBankUserDTO)
        );

        assertEquals("Not enough parameters valid for bank user", exception.getMessage());
        verify(generalUserRepo).findByUsername("bank_user");
        verify(generalUserRepo, never()).save(any());
    }

    @Test
    void createUser_BusinessUser_MissingItemsSold_ThrowsException() {
        // Arrange
        CreateUserDTO invalidBusinessUserDTO = new CreateUserDTO(
            "business_user",
            "password123",
            "BUSINESS",
            null,
            "USA",
            Arrays.asList("Canada", "Mexico"),
            null // Missing items sold
        );
        when(generalUserRepo.findByUsername("business_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authUserService.createUser(invalidBusinessUserDTO)
        );

        assertEquals("Not enough parameters valid for bank user", exception.getMessage());
        verify(generalUserRepo).findByUsername("business_user");
        verify(generalUserRepo, never()).save(any());
    }

    @Test
    void createUser_BusinessUser_MissingDestinationCountries_ThrowsException() {
        // Arrange
        CreateUserDTO invalidBusinessUserDTO = new CreateUserDTO(
            "business_user",
            "password123",
            "BUSINESS",
            null,
            "USA",
            null, // Missing destination countries
            Arrays.asList("Electronics", "Software")
        );
        when(generalUserRepo.findByUsername("business_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authUserService.createUser(invalidBusinessUserDTO)
        );

        assertEquals("Not enough parameters valid for bank user", exception.getMessage());
        verify(generalUserRepo).findByUsername("business_user");
        verify(generalUserRepo, never()).save(any());
    }

    @Test
    void createUser_BusinessUser_MissingOriginCountry_ThrowsException() {
        // Arrange
        CreateUserDTO invalidBusinessUserDTO = new CreateUserDTO(
            "business_user",
            "password123",
            "BUSINESS",
            null,
            null, // Missing origin country
            Arrays.asList("Canada", "Mexico"),
            Arrays.asList("Electronics", "Software")
        );
        when(generalUserRepo.findByUsername("business_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authUserService.createUser(invalidBusinessUserDTO)
        );

        assertEquals("Not enough parameters valid for bank user", exception.getMessage());
        verify(generalUserRepo).findByUsername("business_user");
        verify(generalUserRepo, never()).save(any());
    }

    @Test
    void createUser_InvalidRole_ThrowsException() {
        // Arrange
        CreateUserDTO invalidRoleDTO = new CreateUserDTO(
            "invalid_user",
            "password123",
            "INVALID_ROLE",
            null,
            null,
            null,
            null
        );
        when(generalUserRepo.findByUsername("invalid_user")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authUserService.createUser(invalidRoleDTO)
        );

        assertEquals("User type not available", exception.getMessage());
        verify(generalUserRepo).findByUsername("invalid_user");
        verify(generalUserRepo, never()).save(any());
    }

    @Test
    void createUser_CaseInsensitiveRole_Success() {
        // Arrange
        CreateUserDTO lowerCaseRoleDTO = new CreateUserDTO(
            "member_user",
            "password123",
            "member", // lowercase role
            null,
            null,
            null,
            null
        );
        when(generalUserRepo.findByUsername("member_user")).thenReturn(Optional.empty());

        // Act
        TokenDTO result = authUserService.createUser(lowerCaseRoleDTO);

        // Assert
        assertNotNull(result);
        assertEquals("member_user", result.getUsername());
    verify(generalUserRepo).save(any(User.class));
    }

    @Test
    void createUser_CaseInsensitiveIndustry_Success() {
        // Arrange
        CreateUserDTO lowerCaseIndustryDTO = new CreateUserDTO(
            "bank_user",
            "password123",
            "BANK",
            "technology", // lowercase industry
            "USA",
            null,
            null
        );
        when(generalUserRepo.findByUsername("bank_user")).thenReturn(Optional.empty());

        // Act
        TokenDTO result = authUserService.createUser(lowerCaseIndustryDTO);

        // Assert
        assertNotNull(result);
        assertEquals("bank_user", result.getUsername());
        assertEquals("technology", result.getIndustry());
        verify(generalUserRepo).save(any(BankUser.class));
    }

    @Test
    void createUser_VerifyRoleAssignment() {
        // Arrange
        when(generalUserRepo.findByUsername("member_user")).thenReturn(Optional.empty());

        // Act
        authUserService.createUser(memberUserDTO);

        // Assert
        verify(generalUserRepo).save(argThat(user -> {
            assertEquals(Role.MEMBER, user.getRole());
            return true;
        }));
    }

    @Test
    void createUser_VerifyPasswordHandling() {
        // Arrange
        when(generalUserRepo.findByUsername("member_user")).thenReturn(Optional.empty());

        // Act
        authUserService.createUser(memberUserDTO);

        // Assert
        verify(generalUserRepo).save(argThat(user -> {
            assertEquals("password123", user.getHashedPassword());
            return true;
        }));
    }
}
