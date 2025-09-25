package com.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.user.dto.TokenDTO;
import com.user.generalUser.GeneralUser;
import com.user.generalUser.GeneralUserRepo;
import com.user.security.exception.ApplicationAuthenticationException;
import com.user.security.service.JwtService;
import com.user.security.user.AuthUser;
import com.user.security.enums.Role;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    private final GeneralUserRepo generalUserRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthUserServiceImpl(GeneralUserRepo generalUserRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.generalUserRepo = generalUserRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    public void createUser(String username, String password, String role) {
        Optional<GeneralUser> preCheck = generalUserRepo.findByUsername(username);
        
        if (preCheck.isPresent()) {
            throw new IllegalArgumentException("User with that username already exists");
        }
        
        String passwordHash = passwordEncoder.encode(password);
        
        // gay
        GeneralUser creation = new GeneralUser(username, passwordHash, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
        creation.getRole().add(Role.valueOf(role.toUpperCase()));
        generalUserRepo.save(creation);
    }
    
    public TokenDTO login(String username, String password) {
        GeneralUser user = generalUserRepo.findByUsername(username)
                                          .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            // System.out.println("Wrong password");
            throw new ApplicationAuthenticationException("Incorrect password");
        }
        
        AuthUser authUser = new AuthUser(
            user.getUsername(),
            user.getHashedPassword(),
            user.getRole()
        );
        
        String jwtToken = jwtService.createJwtToken(authUser);
        
        return new TokenDTO(
            username, 
            jwtToken,
            user.getTariffIds()
        );
    }
    
}