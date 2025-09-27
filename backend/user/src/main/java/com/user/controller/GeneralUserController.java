package com.user.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.service.GeneralUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/user")
@RestController
@Tag(name = "General User", description = "Endpoints for general user's query history and pinned tariffs")
public class GeneralUserController {
    private final Logger log = LoggerFactory.getLogger(GeneralUserController.class);
    private final GeneralUserService generalUserService;

    public GeneralUserController(GeneralUserService generalUserService) {
        this.generalUserService = generalUserService;
    }

    // not included in this sprint, will be added in future sprints
    // @PostMapping("/{username}/history/{tariffId}")
    // public ResponseEntity<Map<Integer, Integer>> addHistory(@PathVariable String username,
    //         @PathVariable Integer tariffId) {
    //     try {
    //         Map<Integer, Integer> history = generalUserService.addHistory(username, tariffId);
    //         return ResponseEntity.ok(history);
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body(null);
    //     }
    // }

    // // not included in this sprint, will be added in future sprints
    // @GetMapping("/{username}/history/{tariffId}")
    // public ResponseEntity<Map<Integer, Integer>> getHistory(@PathVariable String username,
    //         @PathVariable Integer tariffId) {
    //     try {
    //         Map<Integer, Integer> history = generalUserService.retrieveHistory(username, tariffId);
    //         return ResponseEntity.ok(history);
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().body(null);
    //     }
    // }

    @Operation(summary = "Add pinned tariff", description = "Pins a tariff for the user. Maximum of 3 tariffs can be pinned.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pinned tariff successfully added", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request. User not found.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cannot pin more than 3 tariffs", content = @Content)
    })
    @PostMapping("/{username}/pinned-tariffs/{tariffId}")
    public ResponseEntity<List<Integer>> addPinnedTariff(@PathVariable String username,
            @PathVariable Integer tariffId) {
        try {
            List<Integer> tariffIds = generalUserService.addPinnedTariff(username, tariffId);
            return ResponseEntity.ok(tariffIds);
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalStateException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(409).build();
        }
    }

    @Operation(summary = "Remove pinned tariff", responses = {
        @ApiResponse(responseCode = "200", description = "Pinned tariff successfully removed", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Bad request. User not found", content = @Content)
    })
    @PostMapping("/{username}/unpinned-tariffs/{tariffId}")
    public ResponseEntity<List<Integer>> removePinnedTariffs(@PathVariable String username,
            @PathVariable Integer tariffId) {
        try {
            List<Integer> tariffIds = generalUserService.removePinnedTariff(username, tariffId);
            return ResponseEntity.ok(tariffIds);
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Test unauthenticated endpoint", description = "Returns a simple string to verify unauthenticated access")
    @ApiResponse(
        responseCode = "200",
        description = "Successful response with plain text message",
        content = @Content(
            mediaType = MediaType.TEXT_PLAIN_VALUE,
            examples = @ExampleObject(value = "Hello from unauthenticated")
        )
    )
    @GetMapping("/testauth/multilevel")
    public String testAuth() {
        return "Hello from authenticated";
    }

    // @GetMapping("/hi/details")
    // public Integer returnUsers() {
    // return 1;
    // }
}
