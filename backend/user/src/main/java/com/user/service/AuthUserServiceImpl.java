package com.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.user.dto.CreateUserDTO;
import com.user.dto.TokenDTO;
import com.user.enums.Industry;
import com.user.enums.Role;
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
                creation = new MemberUser(createUserDTO.username(), passwordHash, new HashMap<>(), new ArrayList<>(),
                        new ArrayList<>());
                break;
            }
            case "BANK": {
                creation = new BankUser(createUserDTO.username(), passwordHash, new HashMap<>(), new ArrayList<>(),
                        Industry.valueOf(createUserDTO.industry().toUpperCase()), createUserDTO.originCountry());
                break;
            }
            case "BUSINESS": {
                creation = new BusinessUser(createUserDTO.username(), passwordHash, new HashMap<>(), new ArrayList<>(),
                        createUserDTO.itemsSold(), createUserDTO.destinationCountries(), createUserDTO.originCountry());
                break;
            }
            case "ADMIN": {
                creation = new User(createUserDTO.username(), passwordHash, new HashMap<>(), new ArrayList<>());
                break;
            }
            default: {
                throw new IllegalArgumentException("User type not available");
            }
        }

        creation.getRole().add(Role.valueOf(createUserDTO.role().toUpperCase()));
        generalUserRepo.save(creation);

        Map<Integer, Integer> sortedMap = creation.getHistory()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue((a, b) -> b - a))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        List<Integer> sortedSet = sortedMap.keySet().stream().collect(Collectors.toCollection(ArrayList::new));

        switch (createUserDTO.role().toUpperCase()) {
            case "MEMBER": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        ((MemberUser) creation).getPinnedTariffId(),
                        sortedSet);
            }
            case "BANK": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        createUserDTO.industry(),
                        createUserDTO.originCountry(),
                        sortedSet);
            }
            case "BUSINESS": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        createUserDTO.itemsSold(),
                        createUserDTO.destinationCountries(),
                        createUserDTO.originCountry(),
                        sortedSet);
            }
            case "ADMIN": {
                return new TokenDTO(
                        createUserDTO.username(),
                        null,
                        sortedSet);
            }
            default: {
                return null;
            }
        }

    }

}
