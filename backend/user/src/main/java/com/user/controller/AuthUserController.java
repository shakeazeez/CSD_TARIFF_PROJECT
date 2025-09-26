package com.user.controller;


import com.user.dto.CreateUserDTO;
import com.user.dto.LoginDTO;
import com.user.dto.TokenDTO;
import com.user.security.exception.ApplicationAuthenticationException;
import com.user.service.AuthUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO loginDTO) { 
        try {
            TokenDTO login = authUserService.login(loginDTO);
            return ResponseEntity.ok(login);
        } catch(ApplicationAuthenticationException | IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }
    
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody CreateUserDTO createUserDTO) {
        try {
            authUserService.createUser(createUserDTO);
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.accepted().build();
    }
    
    @GetMapping("/testauth/multilevel")
    public String testAuth() {
        return "Hello from unauthenticated";
    }
    
}