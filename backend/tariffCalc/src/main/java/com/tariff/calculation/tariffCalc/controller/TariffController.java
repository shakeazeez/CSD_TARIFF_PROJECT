package com.tariff.calculation.tariffCalc.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;
import com.tariff.calculation.tariffCalc.service.TariffCalculationService;
import com.tariff.calculation.tariffCalc.service.TariffOverviewService;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;

@Tag(name = "Tariff Controller", description = "API for tariff calculations and country information")
@RequestMapping("/tariff")
@RestController
public class TariffController {

    private final TariffCalculationService tariffService;
    private final TariffOverviewService tariffOverviewService;

    private final Logger log = Logger.getLogger(TariffController.class.getName());


    @Autowired
    public TariffController(TariffCalculationService tariffService, TariffOverviewService tariffOverviewService) {
        this.tariffService = tariffService;
        this.tariffOverviewService = tariffOverviewService;
    }
    @Operation(summary = "Get all countries", description = "Retrieve a list of all available countries for tariff calculations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved countries"),
        @ApiResponse(responseCode = "404", description = "Countries not found")
    })
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllItems() {
        List<Country> country = null;
        
        try {
            country = tariffOverviewService.getAllCountries();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(country);
    }
    
    /*
     * Get tariff details for item between two countries of default(current) year
     */
    @Operation(summary = "Get current year tariff details", description = "Calculate tariff details for an item between two countries for the current year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated tariff"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Tariff data not found")
    })
    @PostMapping("/current")
    public ResponseEntity<TariffResponseDTO> getCurrentTariffDetails(
            @Parameter(description = "Tariff calculation query parameters", required = true)
            @RequestBody TariffCalculationQueryDTO queryDTO) {

        TariffResponseDTO response = null;
        try {
            response = tariffService.getCurrentTariffDetails(queryDTO);
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (NoSuchElementException e) {
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ApiFailureException e) {
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
    
    /*
     * Get tariff details for item between two countries of selected year
     */
    @Operation(summary = "Get historical tariff details", description = "Get tariff overview and details for an item between two countries for a specific past year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved historical tariff data"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Historical tariff data not found")
    })
    @PostMapping("/past")
    public ResponseEntity<TariffOverviewResponseDTO> getHistoricalTariffDetails(
            @Parameter(description = "Tariff calculation query parameters including year", required = true)
            @RequestBody TariffCalculationQueryDTO queryDTO) {

        TariffOverviewResponseDTO response = null;
        try {
            response = tariffOverviewService.getTariffOverview(queryDTO);
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (NoSuchElementException e) {
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ApiFailureException e) {
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);

    }

    
}
