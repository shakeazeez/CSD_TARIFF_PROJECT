package com.tariff.calculation.tariffCalc.controller;

import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.tariff.calculation.tariffCalc.dto.TariffCalculationQueryDTO;
import com.tariff.calculation.tariffCalc.service.TariffCalculationService;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;
import com.tariff.calculation.tariffCalc.exception.ApiFailureException;
import com.tariff.calculation.tariffCalc.item.Item;


@RequestMapping("/tariff")
@RestController
public class TariffController {

    private final TariffCalculationService tariffService;

    private final Logger log = Logger.getLogger(TariffController.class.getName());

    @Autowired
    public TariffController(TariffCalculationService tariffService) {
        this.tariffService = tariffService;
    }

    /*
     * Get tariff details for item between two countries of default(current) year
     */
    @GetMapping("/current")
    public ResponseEntity<TariffResponseDTO> getCurrentTariffDetails(@RequestBody TariffCalculationQueryDTO queryDTO) {

        TariffResponseDTO response = null;
        try {
            response = tariffService.getCurrentTariffDetails(queryDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
    
    /*
     * Get tariff details for item between two countries of selected year
     */
    @GetMapping("/past")
    public ResponseEntity<TariffResponseDTO> getHistoricalTariffDetails(@RequestBody TariffCalculationQueryDTO queryDTO) {

        TariffResponseDTO response = null;
        try {
            response = tariffService.getPastTariffDetails(queryDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);

    }

    
}
