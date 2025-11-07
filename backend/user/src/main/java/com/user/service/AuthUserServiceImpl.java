package com.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.user.dto.CreateUserDTO;
import com.user.dto.TokenDTO;
import com.user.enums.Industry;
import com.user.enums.Role;
import com.user.user.User;
import com.user.user.BankUser;
import com.user.user.BusinessUser;
import com.user.user.MemberUser;
import com.user.user.UserRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    private final UserRepo generalUserRepo;
    private final Logger log = LoggerFactory.getLogger(AuthUserServiceImpl.class);

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
            case "BANK": {
                if (createUserDTO.industry() == null || createUserDTO.originCountry() == null) {
                    throw new IllegalArgumentException("Not enough parameters valid for bank user");
                }
                creation = new BankUser(createUserDTO.username(), passwordHash,
                        Role.valueOf(createUserDTO.role().toUpperCase()),
                        Industry.valueOf(createUserDTO.industry().toUpperCase()), createUserDTO.originCountry());
                break;
            }
            case "BUSINESS": {
                if (createUserDTO.itemsSold() == null || createUserDTO.destinationCountries() == null
                        || createUserDTO.originCountry() == null) {
                    throw new IllegalArgumentException("Not enough parameters valid for bank user");
                }
                log.info(createUserDTO.toString());
                creation = new BusinessUser(createUserDTO.username(), passwordHash,
                        Role.valueOf(createUserDTO.role().toUpperCase()),
                        createUserDTO.itemsSold(), createUserDTO.destinationCountries(), createUserDTO.originCountry());
                break;
            }
            case "ADMIN": {
                creation = new User(createUserDTO.username(), passwordHash,
                        Role.valueOf(createUserDTO.role().toUpperCase()));
                break;
            }
            case "MEMBER": {
                creation = new User(createUserDTO.username(), passwordHash,
                        Role.valueOf(createUserDTO.role().toUpperCase()));
                break;
            }
            default: {
                throw new IllegalArgumentException("User type not available");
            }
        }
        generalUserRepo.save(creation);

        switch (createUserDTO.role().toUpperCase()) {
            case "MEMBER": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        creation.getPinnedTariffId(),
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
