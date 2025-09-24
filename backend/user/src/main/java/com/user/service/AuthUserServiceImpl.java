package com.user.service;

import com.user.dto.TokenDTO;
import com.user.generalUser.GeneralUser;
import com.user.generalUser.GeneralUserRepo;
import com.user.security.AuthUser;
import com.user.security.exception.ApplicationAuthenticationException;
import com.user.security.service.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthUserServiceImpl implements AuthUserService {
    
    private final GeneralUserRepo generalUserRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthUserServiceImpl(GeneralUserRepo generalUserRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.generalUserRepo = generalUserRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    public TokenDTO login(String username, String password) {
        GeneralUser user = generalUserRepo.findByUsername(username)
                                          .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
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