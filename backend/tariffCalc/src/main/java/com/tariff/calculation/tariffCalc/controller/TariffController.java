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

import com.tariff.calculation.tariffCalc.country.Country;
import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewQueryDTO;
import com.tariff.calculation.tariffCalc.dto.TariffOverviewResponseDTO;
import com.tariff.calculation.tariffCalc.service.TariffCalculationService;
import com.tariff.calculation.tariffCalc.service.TariffOverviewService;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;


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
    @PostMapping("/current")
    public ResponseEntity<TariffResponseDTO> getCurrentTariffDetails(@RequestBody TariffCalculationQueryDTO queryDTO) {

        TariffResponseDTO response = null;
        try {
            response = tariffService.getCurrentTariffDetails(queryDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ApiFailureException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
    
    /*
     * Get tariff details for item between two countries of selected year
     */
    @PostMapping("/past")
    public ResponseEntity<TariffOverviewResponseDTO> getHistoricalTariffDetails(@RequestBody TariffOverviewQueryDTO queryDTO) {

        TariffOverviewResponseDTO response = null;
        try {
            response = tariffOverviewService.getTariffOverview(queryDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ApiFailureException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);

    }

    
}
