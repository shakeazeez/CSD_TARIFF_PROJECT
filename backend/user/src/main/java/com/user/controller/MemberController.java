package com.user.controller;

import java.util.List;

import com.user.service.MemberUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/member")
public class MemberController {
	
    private final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberUserService memberUserService;
    
    public MemberController(MemberUserService memberUserService) {
        this.memberUserService = memberUserService;
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
            List<Integer> tariffIds = memberUserService.addPinnedTariff(username, tariffId);
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
            List<Integer> tariffIds = memberUserService.removePinnedTariff(username, tariffId);
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
			return ResponseEntity.ok(memberUserService.getPinnedTariff(username));
		} catch (IllegalAccessError e) {
		    log.info(e.getMessage());
		    return ResponseEntity.status(403).build();
		} catch (IllegalArgumentException e) {
		    log.info(e.getMessage());
		    return ResponseEntity.status(404).build();
		}
	}
}