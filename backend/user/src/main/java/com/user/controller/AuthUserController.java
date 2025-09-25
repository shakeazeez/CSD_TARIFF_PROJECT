package com.user.controller;


import com.user.dto.TokenDTO;
import com.user.security.exception.ApplicationAuthenticationException;
import com.user.service.AuthUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/auth")
public class AuthUserController {
    
    private final AuthUserService authUserService;
    private final Logger log = LoggerFactory.getLogger(GeneralUserController.class);    
    
    public AuthUserController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }
    
    
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestParam String username, @RequestParam String password) { 
        try {
            TokenDTO login = authUserService.login(username, password);
            return ResponseEntity.ok(login);
        } catch(ApplicationAuthenticationException | IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }
    
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String role) {
        
        try {
            authUserService.createUser(username, password, role);
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.accepted().build();
    }
}