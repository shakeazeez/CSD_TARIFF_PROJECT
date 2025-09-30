package com.tariff.calculation.tariffCalc.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.dto.GeneralTariffDTO;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;
import com.tariff.calculation.tariffCalc.service.TariffCalculationService;
import com.tariff.calculation.tariffCalc.service.TariffOverviewService;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.tariff.TariffRepo;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;

@Tag(name = "Tariff Controller", description = "Tariff calculation and overview endpoints")
@RequestMapping("/tariff")
@RestController
public class TariffController {

    private final TariffCalculationService tariffService;
    private final TariffOverviewService tariffOverviewService;
    private final TariffRepo tariffRepo;

    private final Logger log = Logger.getLogger(TariffController.class.getName());

    @Autowired
    public TariffController(TariffCalculationService tariffService, TariffOverviewService tariffOverviewService, TariffRepo tariffRepo) {
        this.tariffService = tariffService;
        this.tariffOverviewService = tariffOverviewService;
        this.tariffRepo = tariffRepo;
    }

    @Operation(summary = "Get all countries", description = "Retrieve a list of all available countries for tariff calculations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved countries", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Country.class))
            }),
            @ApiResponse(responseCode = "404", description = "Countries not found", content = @Content)
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
            @ApiResponse(responseCode = "200", description = "Successfully calculated tariff", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TariffResponseDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tariff data not found", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Tariff calculation query parameters", 
        required = true, content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = TariffCalculationQueryDTO.class), 
            examples = @ExampleObject(value = "{ \"reportingCountry\": \"China\", \"partnerCountry\": \"India\", \"item\": \"Slipper\", \"itemCost\": 1000.0 }")
    ))
    @PostMapping("/current")
    public ResponseEntity<TariffResponseDTO> getCurrentTariffDetails(
            @Parameter(description = "Tariff calculation query parameters", required = true) @RequestBody TariffCalculationQueryDTO queryDTO) {

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
        } catch (Exception e) {
            log.info(e.getMessage()); 
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all/tariff")
    public List<Tariff> getAllTariff() {
        return tariffRepo.findAll();
    }
    /*
     * Get tariff details for item between two countries of selected year
     */
    @Operation(summary = "Get historical tariff details", description = "Get tariff overview for an item between two countries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved historical tariff data", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TariffOverviewResponseDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "404", description = "Historical tariff data not found", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Tariff calculation query parameters", 
        required = true, content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = TariffCalculationQueryDTO.class), 
            examples = @ExampleObject(value = "{ \"reportingCountry\": \"China\", \"partnerCountry\": \"India\", \"item\": \"Slipper\", \"itemCost\": 1000.0 }")
    ))
    @PostMapping("/past")
    public ResponseEntity<TariffOverviewResponseDTO> getHistoricalTariffDetails(
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
        } catch (Exception e) {
            log.info(e.getMessage()); 
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Get current tariff by id", description = "Fetches the tariff details for the given tariff id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current tariff found and returned", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralTariffDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid tariff id given", content = @Content)
    })
    @PostMapping("/current/{id}")
    public ResponseEntity<GeneralTariffDTO> getCurrentTariffById(
            @Parameter(description = "Unique tariff id", required = true) @PathVariable Integer id) {
        try {
            return ResponseEntity.ok(tariffService.getTariffById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.info(e.getMessage()); 
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get historical tariff by id", description = "Fetches all historical tariff records for the given tariff id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of historical tariffs returned", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = GeneralTariffDTO.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid tariff id given", content = @Content)
    })
    @PostMapping("/past/{id}")
    public ResponseEntity<List<GeneralTariffDTO>> getPastTariffById(
            @Parameter(description = "Unique tariff id", required = true) @PathVariable Integer id) {
        try {
            return ResponseEntity.ok(tariffOverviewService.getAllTariff(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.info(e.getMessage()); 
            return ResponseEntity.internalServerError().build();
        }
    }

}
