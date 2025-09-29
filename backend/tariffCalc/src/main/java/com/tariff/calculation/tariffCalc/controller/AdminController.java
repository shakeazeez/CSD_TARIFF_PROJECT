package com.tariff.calculation.tariffCalc.controller;

import com.tariff.calculation.tariffCalc.dto.TariffDeleteDTO;
import com.tariff.calculation.tariffCalc.service.CrudService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tariff/admin")
public class AdminController {
    private final CrudService crudService;
    
    public AdminController(CrudService crudService) {
        this.crudService = crudService;
    }
    
    @Operation(summary = "Delete item by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item deleted", content = @Content()),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content())
    })
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable int itemId) {
        try {
            crudService.deleteItem(itemId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete country by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Country deleted", content = @Content()),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content())
    })
    @DeleteMapping("/country/{countryId}")
    public ResponseEntity<?> deleteCountry(@PathVariable int countryId) {
        try {
            crudService.deleteCountry(countryId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Delete tariff by DTO")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "DTO for tariff deletion",
        required = true,
        content = @Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TariffDeleteDTO.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tariff deleted", content = @Content()),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content())
    })
    @DeleteMapping("/tariff")
    public ResponseEntity<?> deleteItem(@RequestBody TariffDeleteDTO tariffDeleteDTO) {
        try {
            crudService.deleteTariff(tariffDeleteDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}