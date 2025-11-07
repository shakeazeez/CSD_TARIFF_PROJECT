package com.user.controller;

import java.time.LocalDate;
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

import com.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final UserService userService;

    public GeneralUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Add tariff to user history", 
        description = "Add a tariff calculation to the user's query history"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tariff added to history successfully", 
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid username or tariff ID", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - user mismatch", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{username}/history/{tariffId}")
    public ResponseEntity<Map<Integer, LocalDate>> addHistory(
            @Parameter(description = "Username to add history for", required = true)
            @PathVariable String username,
            @Parameter(description = "Tariff Id to add to history", required = true)
            @PathVariable Integer tariffId) {
        try {
            Map<Integer, LocalDate> history = userService.addHistory(username, tariffId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(
        summary = "Get user query history", 
        description = "Retrieve top 5 tariff calculations in the user's query history with timestamps"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User history retrieved successfully", 
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid username provided", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - user mismatch", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{username}/history")
    public ResponseEntity<Map<Integer, LocalDate>> getHistory(
            @Parameter(description = "Username to retrieve history for", required = true)
            @PathVariable String username) {
        try {
            Map<Integer, LocalDate> history = userService.retrieveHistory(username, 5);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @Operation(
        summary = "Get information for csv", 
        description = "Retrieve all tariff calculations in the user's query history with timestamps"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User history retrieved successfully", 
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid username provided", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied - user mismatch", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{username}/csv/")
    public ResponseEntity<Map<Integer, LocalDate>> getHistoryDownload(
            @Parameter(description = "Username to retrieve history for", required = true)
            @PathVariable String username
    ) {
        try {
            Map<Integer, LocalDate> history = userService.retrieveHistory(username, Integer.MAX_VALUE);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
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
               List<Integer> tariffIds = userService.addPinnedTariff(username, tariffId);
               return ResponseEntity.ok(tariffIds);
           } catch (IllegalArgumentException e) {
               log.info(e.getMessage());
               return ResponseEntity.badRequest().body(null);
           } catch (IllegalStateException e) {
               log.info(e.getMessage());
               return ResponseEntity.status(409).build();
           } catch (Exception e) {
               log.info(e.getMessage()); 
               return ResponseEntity.internalServerError().build();
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
               List<Integer> tariffIds = userService.removePinnedTariff(username, tariffId);
               return ResponseEntity.ok(tariffIds);
           } catch (IllegalArgumentException e) {
               log.info(e.getMessage());
               return ResponseEntity.badRequest().build();
           } catch (Exception e) {
               log.info(e.getMessage()); 
               return ResponseEntity.internalServerError().build();
           }
       }
       
       @GetMapping("/{username}")
       public ResponseEntity<?> getPinnedTariff(@PathVariable String username) {
           try {
			return ResponseEntity.ok(userService.getPinnedTariff(username));
		} catch (IllegalAccessError e) {
		    log.info(e.getMessage());
		    return ResponseEntity.status(403).build();
		} catch (IllegalArgumentException e) {
		    log.info(e.getMessage());
		    return ResponseEntity.status(404).build();
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
}
