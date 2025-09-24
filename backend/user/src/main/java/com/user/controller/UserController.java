package com.user.controller;

import com.user.service.AuthUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    private final AuthUserService authUserService;
    
    public UserController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }
    
    @PostMapping
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            authUserService.login(username, password);
        } catch(Exception e) {
            return ResponseEntity.status(401).build();
        }
        
        return ResponseEntity.accepted().build();
    }
}