package com.tariff.calculation.tariffCalc.dto;

import java.time.LocalDate;

/*
 * DTO for tariff request.
 */
public record TariffCalculationQueryDTO (
    String reportingCountry,
    String partnerCountry,
    String item, 
    Double itemCost,
    LocalDate effectiveDate // for quering historical tariffs
) 
{}