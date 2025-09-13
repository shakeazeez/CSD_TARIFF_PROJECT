package com.tariff.calculation.tariffCalc.dto;

import java.time.LocalDate;

/*
 * DTO for user to query tariff overview for a specific period.
 * TODO: change if needed.
 */
public record TariffOverviewQueryDTO (
    String reportingCountry,
    String partnerCountry,
    String item, 
    LocalDate startPeriod,
    LocalDate endPeriod
) {}
