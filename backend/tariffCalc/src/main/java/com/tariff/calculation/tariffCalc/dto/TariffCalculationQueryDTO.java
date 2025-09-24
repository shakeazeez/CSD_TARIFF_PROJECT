package com.tariff.calculation.tariffCalc.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * DTO for tariff request.
 */
public record TariffCalculationQueryDTO (
    String reportingCountry,
    String partnerCountry,
    
    @Pattern(regexp = "[a-zA-Z0-9]+", message="Only characters allowed") @Size(max=100)
    String item, 
    Double itemCost
) 
{}