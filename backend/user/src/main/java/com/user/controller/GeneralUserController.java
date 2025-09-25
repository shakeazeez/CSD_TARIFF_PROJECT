package com.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.user.dto.TokenDTO;
import com.user.security.exception.ApplicationAuthenticationException;
import com.user.service.AuthUserService;
import com.user.service.GeneralUserService;


@RequestMapping("/user")
@RestController
public class GeneralUserController {
    private final GeneralUserService generalUserService;
    private final AuthUserService authUserService;

    public GeneralUserController(GeneralUserService generalUserService, AuthUserService authUserService) {
        this.generalUserService = generalUserService;
        this.authUserService = authUserService;
    }

    @PostMapping("/{username}/history/{tariffId}")
    public ResponseEntity<Map<Integer, Integer>> addHistory(@PathVariable String username, @PathVariable Integer tariffId) {
        try {
            Map<Integer, Integer> history = generalUserService.addHistory(username, tariffId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{username}/history/{tariffId}")
    public ResponseEntity<Map<Integer, Integer>> getHistory(@PathVariable String username, @PathVariable Integer tariffId) {
        try {
            Map<Integer, Integer> history = generalUserService.retrieveHistory(username, tariffId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{userId}/pinned-tariffs/{tariffId}")
    public ResponseEntity<List<Integer>> addPinnedTariff(@PathVariable Integer userId, @PathVariable Integer tariffId) {
        try {
            List<Integer> tariffIds = generalUserService.addPinnedTariff(userId, tariffId);
            return ResponseEntity.ok(tariffIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/{userId}/unpinned-tariffs/{tariffId}")
    public ResponseEntity<List<Integer>> removePinnedTariffs(@PathVariable Integer userId, @PathVariable Integer tariffId) {
        try {
            List<Integer> tariffIds = generalUserService.removePinnedTariff(userId, tariffId);
            return ResponseEntity.ok(tariffIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
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
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.accepted().build();
    }
    
    // @GetMapping("/hi/details")
    // public Integer returnUsers() {
    //     return 1;
    // }
}
