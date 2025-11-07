package com.user.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.user.dto.CreateUserDTO;
import com.user.dto.TokenDTO;
import com.user.enums.Industry;
import com.user.enums.Role;
import com.user.history.History;
import com.user.user.User;
import com.user.user.BankUser;
import com.user.user.BusinessUser;
import com.user.user.MemberUser;
import com.user.user.UserRepo;

import org.springframework.stereotype.Service;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    private final UserRepo generalUserRepo;

    public AuthUserServiceImpl(UserRepo generalUserRepo) {
        this.generalUserRepo = generalUserRepo;
    }

    public TokenDTO createUser(CreateUserDTO createUserDTO) {
        Optional<User> preCheck = generalUserRepo.findByUsername(createUserDTO.username());
        if (preCheck.isPresent()) {
            throw new IllegalArgumentException("User with that username already exists");
        }

        // String passwordHash = passwordEncoder.encode(createUserDTO.password());
        String passwordHash = createUserDTO.password();

        // Note creation left as error as to be changed depending on type
        User creation;

        switch (createUserDTO.role().toUpperCase()) {
            case "MEMBER": {
                creation = new MemberUser(createUserDTO.username(), passwordHash, new ArrayList<>(),
                        new ArrayList<>());
                break;
            }
            case "BANK": {
                if (createUserDTO.industry() == null || createUserDTO.originCountry() == null) {
                    throw new IllegalArgumentException("Not enough parameters valid for bank user");
                } 
                creation = new BankUser(createUserDTO.username(), passwordHash, new ArrayList<>(),
                        Industry.valueOf(createUserDTO.industry().toUpperCase()), createUserDTO.originCountry());
                break;
            }
            case "BUSINESS": {
                if (createUserDTO.itemsSold() == null || createUserDTO.destinationCountries() == null ||  createUserDTO.originCountry() == null) {
                    throw new IllegalArgumentException("Not enough parameters valid for bank user");
                } 
                creation = new BusinessUser(createUserDTO.username(), passwordHash, new ArrayList<>(),
                        createUserDTO.itemsSold(), createUserDTO.destinationCountries(), createUserDTO.originCountry());
                break;
            }
            case "ADMIN": {
                creation = new User(createUserDTO.username(), passwordHash, new ArrayList<>());
                break;
            }
            default: {
                throw new IllegalArgumentException("User type not available");
            }
        }

        creation.getRole().add(Role.valueOf(createUserDTO.role().toUpperCase()));
        generalUserRepo.save(creation);

        switch (createUserDTO.role().toUpperCase()) {
            case "MEMBER": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        ((MemberUser) creation).getPinnedTariffId(),
                        new HashMap<>());
            }
            case "BANK": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        createUserDTO.industry(),
                        createUserDTO.originCountry(),
                        new HashMap<>());
            }
            case "BUSINESS": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        createUserDTO.itemsSold(),
                        createUserDTO.destinationCountries(),
                        createUserDTO.originCountry(),
                        new HashMap<>());
            }
            case "ADMIN": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        new HashMap<>());
            }
            default: {
                return null;
            }
        }

    }

}
