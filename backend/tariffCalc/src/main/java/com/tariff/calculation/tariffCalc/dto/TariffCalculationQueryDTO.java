package com.tariff.calculation.tariffCalc.dto;

/*
 * DTO for tariff request.
 */
public record TariffCalculationQueryDTO (
    String reportingCountry,
    String partnerCountry,
    String item, 
    Double itemCost
) 
{}