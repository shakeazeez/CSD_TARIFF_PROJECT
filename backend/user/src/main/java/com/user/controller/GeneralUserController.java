package com.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.service.GeneralUserService;


@RequestMapping("/user")
@RestController
public class GeneralUserController {
    private final GeneralUserService generalUserService;

    public GeneralUserController(GeneralUserService generalUserService) {
        this.generalUserService = generalUserService;
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

    @PostMapping("/{username}/pinned-tariffs/{tariffId}")
    public ResponseEntity<List<Integer>> addPinnedTariff(@PathVariable String username, @PathVariable Integer tariffId) {
        try {
            List<Integer> tariffIds = generalUserService.addPinnedTariff(username, tariffId);
            return ResponseEntity.ok(tariffIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/{username}/unpinned-tariffs/{tariffId}")
    public ResponseEntity<List<Integer>> removePinnedTariffs(@PathVariable String username, @PathVariable Integer tariffId) {
        try {
            List<Integer> tariffIds = generalUserService.removePinnedTariff(username, tariffId);
            return ResponseEntity.ok(tariffIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // @GetMapping("/hi/details")
    // public Integer returnUsers() {
    //     return 1;
    // }
}
