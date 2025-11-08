package com.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.user.dto.CreateUserDTO;
import com.user.dto.TokenDTO;
import com.user.enums.Industry;
import com.user.enums.Role;
import com.user.user.User;
import com.user.user.BankUser;
import com.user.user.BusinessDetails;
import com.user.user.BusinessDetailsRepo;
import com.user.user.BusinessUser;
import com.user.user.MemberUser;
import com.user.user.UserRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    private final UserRepo generalUserRepo;
    private final BusinessDetailsRepo businessDetailsRepo;
    private final Logger log = LoggerFactory.getLogger(AuthUserServiceImpl.class);

    public AuthUserServiceImpl(UserRepo generalUserRepo, BusinessDetailsRepo businessDetailsRepo) {
        this.generalUserRepo = generalUserRepo;
        this.businessDetailsRepo = businessDetailsRepo;
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
                log.info(createUserDTO.toString());
                if (createUserDTO.tariffs() == null || createUserDTO.originCountry() == null) {
                    throw new IllegalArgumentException("Not enough parameters valid for bank user");
                }
                
                Set<BusinessDetails> tariffs = createUserDTO.tariffs().stream().map(a ->
                    businessDetailsRepo.findByReportingCountryAndItemIgnoreCase(a.reportingCountry(), a.item())
                        .orElseGet(() -> businessDetailsRepo.save(new BusinessDetails(a.reportingCountry(), a.item())))
                ).collect(Collectors.toCollection(HashSet::new));

                log.info(createUserDTO.toString());
                creation = new BusinessUser(createUserDTO.username(), passwordHash,
                        Role.valueOf(createUserDTO.role().toUpperCase()),
                        tariffs, createUserDTO.originCountry());
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
                        createUserDTO.tariffs(),
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
