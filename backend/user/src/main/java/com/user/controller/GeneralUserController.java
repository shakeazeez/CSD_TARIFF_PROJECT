package com.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
}
