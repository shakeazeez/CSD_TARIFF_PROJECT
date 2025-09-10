package com.tariff.calculation.tariffCalc.controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tariff.calculation.tariffCalc.dto.TariffQueryDTO;
import com.tariff.calculation.tariffCalc.service.TariffService;
import com.tariff.calculation.tariffCalc.tariff.Tariff;
import com.tariff.calculation.tariffCalc.dto.TariffResponseDTO;

@RequestMapping("/tariff")
@RestController
public class TariffController {
    private final TariffService tariffService;

    private final Logger log = Logger.getLogger(TariffController.class.getName());

    @Autowired
    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    /**
     * Calculates tariff details based on the provided query parameters.
     *
     * @param reportingCountry The country reporting the tariff.
     * @param partnerCountry The partner country involved in the trade.
     * @param item The item for which the tariff is calculated.
     * @param costOfItem The cost of the item.
     * @return A ResponseEntity containing the tariff details.
     */
    @GetMapping("/calculate")
    public ResponseEntity<TariffResponseDTO> getTariffDetails (

        // request parameters for the query
        @RequestParam String reportingCountry,
        @RequestParam String partnerCountry,
        @RequestParam String item,
        @RequestParam Integer costOfItem) {
        
        TariffQueryDTO queryDTO = new TariffQueryDTO(
            reportingCountry, partnerCountry, item, costOfItem
        );

        log.info("Received tariff calculation request: " + queryDTO);

        // getTariffDetails() method not implemented yet (for visualization purpose only)
        return ResponseEntity.ok(tariffService.getTariffDetails(queryDTO));

        // TODO: include exception handling (e.g. invalid parameters based on TariffServiceImpl)
        
    }
    
}
