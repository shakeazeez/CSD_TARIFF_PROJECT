package com.user.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.service.MemberUserService;
import com.user.service.UserService;
import com.user.user.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/user")
@RestController
@Tag(name = "General User", description = "Endpoints for general user's query history and pinned tariffs")
public class GeneralUserController {
    private final Logger log = LoggerFactory.getLogger(GeneralUserController.class);
    private final UserService userService;
    private final MemberUserService memberUserService;

    public GeneralUserController(UserService userService, MemberUserService memberUserService) {
        this.userService = userService;
        this.memberUserService = memberUserService;
    }

    @PostMapping("/{username}/history/{tariffId}")
    public ResponseEntity<List<Integer>> addHistory(@PathVariable String username,
            @PathVariable Integer tariffId) {
        try {
            List<Integer> history = userService.addHistory(username, tariffId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{username}/history")
    public ResponseEntity<List<Integer>> getHistory(@PathVariable String username) {
        try {
            List<Integer> history = userService.retrieveHistory(username);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
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

    // REMOVE THs
    @GetMapping("/all")
    public List<User> returnAllUsers() {
        return userService.getAllUsers();
    }
}
