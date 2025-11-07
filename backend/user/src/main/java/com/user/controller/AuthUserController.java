package com.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.dto.CreateUserDTO;
import com.user.dto.TokenDTO;
import com.user.service.AuthUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "Endpoints for user authentication")
@RestController()
@RequestMapping("/auth")
public class AuthUserController {

    private final AuthUserService authUserService;
    private final Logger log = LoggerFactory.getLogger(GeneralUserController.class);

    public AuthUserController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    @Operation(summary = "Register user", responses = {
        @ApiResponse(responseCode = "200", description = "Registration successful, returns authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDTO.class))),
        @ApiResponse(responseCode = "409", description = "User with that username already exists", content = @Content),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration details", 
        required = true, content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = CreateUserDTO.class))
    )
    @PostMapping("/register")
    public ResponseEntity<TokenDTO> registerUser(@RequestBody CreateUserDTO createUserDTO) {
        try {
            return ResponseEntity.ok(authUserService.createUser(createUserDTO));
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
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
        return "Hello from unauthenticated";
    }

}